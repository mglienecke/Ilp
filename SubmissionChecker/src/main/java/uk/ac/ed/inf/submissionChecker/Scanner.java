package uk.ac.ed.inf.submissionChecker;

import com.google.gson.Gson;
import uk.ac.ed.inf.submissionChecker.commands.CommandType;
import uk.ac.ed.inf.submissionChecker.commands.ISubmissionCheckerCommand;
import uk.ac.ed.inf.submissionChecker.config.SubmissionCheckerConfig;

import java.io.*;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * the main entry point for the submission checker to scan directories for submissions and then execute the necessary commands
 */
public class Scanner {

    /**
     * pattern for the report file
     */
    // public static String ReportFileName = "report_%s.html";
    public static String baseDirectory;


    /**
     * launching the scanner using the config file and a base directory
     *
     * @param args
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Scanner config_file base_directory ");
            System.err.println("Example: Scanner taskstorun.json d:\\IlpSubmissions");
            System.exit(1);
        }

        var configFileName = args[0];
        if (Paths.get(configFileName).toFile().exists() == false) {
            System.err.println("The file: " + configFileName + " does not exist");
            System.exit(2);
        }

        /**
         * load the runtime configuration from JSON
         */
        SubmissionCheckerConfig runtimeConfiguration = null;
        Gson config = new Gson();
        try (Reader reader = new FileReader(configFileName)) {
            runtimeConfiguration = config.fromJson(reader, SubmissionCheckerConfig.class);
        } catch (IOException e) {
            System.err.println("error loading the configuration: " + e);
            System.exit(3);
        }


        baseDirectory = args[1];
        if (Paths.get(baseDirectory).toFile().exists() == false) {
            System.err.println("The base directory: " + baseDirectory + " does not exist");
            System.exit(3);
        }

        if (System.getenv("JAVA_HOME") == null) {
            System.err.println("JAVA_HOME is undefined");
            System.exit(2);
        }

        /**
         * traverse all sub-directories below the base directory and search for pom.xml files for the solution
         */
        List<Path> pomFileDirectories = getSubmissionDirectories(baseDirectory);

        /**
         * Some checking
         *
         * First write all directories which have no pom.xml -> there is no proper solution
         * Then write all directories where several entries are present
         *
         * Both will be removed afterwards
         */
        var submissionDirectories = Files.list(Paths.get(baseDirectory)).filter(d -> Files.isDirectory(d.toAbsolutePath())).map(e -> e.getParent().relativize(e)).toList();
        List<ISubmissionCheckerCommand> commandList = List.of(runtimeConfiguration.commandsToExecute());
        List<Path> errorPomDirs = new ArrayList<>();
        List<Path> missingJarPomDirs = new ArrayList<>();

        /**
         * traverse all directories and execute the commands in the configuration. Usually this would be clean and build the package -> then check for the JAR file (class command)
         */
        for (Path pomDir : submissionDirectories) {

            String currentDir = Paths.get(baseDirectory).resolve(pomDir).toAbsolutePath().toString();

            System.out.println("\n\n>>>>>>>>>>>>>>>>>>> Processing: " + pomDir);
            HtmlReportWriter reportWriter;
            String submissionReportName = null;
            try {
                // take the first entry in the path for the directory
                var submissionDir = pomDir.getName(0);
                submissionReportName = String.format(runtimeConfiguration.submissionReportPattern, submissionDir);
                reportWriter = new HtmlReportWriter(Path.of(baseDirectory, submissionReportName), "Submission Report for " + submissionDir, runtimeConfiguration.reportTemplateFile);
            } catch (IOException e) {
                System.err.println("error creating report file: " + submissionReportName);
                System.err.println(e);
                continue;
            }

            /**
             * traverse the commands and write to the file
             */
            for (var currentCommand : commandList) {
                try {
                    if (currentCommand.checkForDependencies(currentDir) == false) {
                        System.err.format("Skipped execution as (one of) the file(s): %s does not exist\n", String.join(", ", currentCommand.getDependencyFiles()));
                        missingJarPomDirs.add(pomDir);
                        break;
                    }

                    var result = currentCommand.execute(currentDir, reportWriter);
                    if (result != 0) {
                        String error = String.format("ERR %d for %s >>> while executing: %s", result, currentDir, currentCommand.getCommandDescription());
                        System.err.println(error);
                        reportWriter.writeln(error);

                        // remember for later
                        errorPomDirs.add(pomDir);
                    } else {
                        System.out.println("Executed : " + currentCommand.getCommandDescription());
                    }
                } catch (Exception e) {
                    System.err.println("EXCEPTION: " + pomDir.toString());
                    System.err.println(e);
                }
            }

            reportWriter.appendJavaSourceFiles(currentDir, getJavaFilesInSubmissionDirectory(currentDir));

            // write the final report
            try {
                // is there any command which is a class command and thus would generate a functional result table
                reportWriter.writeReport(commandList.stream().anyMatch(c -> c.getCommandType() == CommandType.ClassExecution));
                System.out.println("Report written to: " + reportWriter.getReportFileName());
            } catch (IOException e) {
                System.err.println("Error writing the report: " + e);
            }
        }

        System.out.flush();
        System.err.flush();

        Map<Path, List<Path>> solutionsInBaseDirectoryMap = pomFileDirectories.stream().map(e -> e).collect(Collectors.groupingBy(Path::normalize));

        // needed as stream processing with lambdas requires the object to be final
        var testDirectories = pomFileDirectories.stream().toList();
        var directoriesWithoutSolution = submissionDirectories
                .stream()
                .filter(d -> testDirectories
                        .stream()
                        .anyMatch(pd -> pd.toString().contains(d.toString())) == false)
                .toList();


        var directoriesWithSeveralSolutions = pomFileDirectories.stream()
                // this prevents null entries further down if the pom.xml is directly below the root
                .map(p -> p.getParent() != null ? p.getParent() : p)
                .collect(Collectors.groupingBy(e -> e.toString()))
                .entrySet()
                .stream()
                .filter(e -> e.getValue().size() > 1)
                .map(e -> e.getKey())
                .toList();


        errorPomDirs = errorPomDirs.stream().distinct().toList();
        if (errorPomDirs.size() > 0) {
            System.err.println("\n\nList of erroneous submission directories:\n");
            errorPomDirs.forEach(p -> System.err.println(p.toString()));
        }


        for (Path directoryWithoutSolution : directoriesWithoutSolution) {
            System.err.println("ERR >>> " + directoryWithoutSolution.toString() + " has no pom.xml - entries are not processed");
        }

        for (var entry : directoriesWithSeveralSolutions) {
            System.err.println("ERR >>> " + entry + " has several pom.xml (solutions) - entries are not processed");
            pomFileDirectories = pomFileDirectories.stream().filter(d -> d.toString().contains(entry) == false).toList();
        }

        if (missingJarPomDirs.size() > 0){
            System.err.println("Directories without JAR-file");
            for (Path directoryWithoutJar : missingJarPomDirs) {
                System.err.println(directoryWithoutJar.toString());
            }
        }
    }

    /**
     * @return a list of all directories containing pom.xml which have to be built
     */
    public static List<Path> getSubmissionDirectories(String basePath) {
        List<Path> pomFileDirectories = new ArrayList<>();

        Path base = Paths.get(basePath);
        try (Stream<Path> stream = Files.walk(base, Integer.MAX_VALUE)) {
            // create the relative path between the base path and the found path -> this is the way to the sub-directory
            // the maven compiler plugin is rather nasty with 105 pom.xml so we ignore it
            pomFileDirectories = stream
                    .filter(e -> e.getFileName().toString().equalsIgnoreCase("pom.xml") && (e.toString().contains("maven-compiler-plugin") == false))
                    .map(e -> base.relativize(e.getParent().normalize())).toList();
        } catch (IOException ioEx) {
            System.err.println(ioEx);
        }

        return pomFileDirectories;
    }


    /**
     * @return a list of all directories containing pom.xml which have to be built
     */
    public static List<Path> getJavaFilesInSubmissionDirectory(String basePath) {
        List<Path> javaFiles = new ArrayList<>();

        Path base = Paths.get(basePath);
        try (Stream<Path> stream = Files.walk(base, Integer.MAX_VALUE)) {
            javaFiles = stream.filter(e -> e.getFileName().toString().toLowerCase().endsWith(".java")).map(e -> base.relativize(e.normalize())).toList();
        } catch (IOException ioEx) {
            System.err.println(ioEx);
        }

        return javaFiles;
    }


}


