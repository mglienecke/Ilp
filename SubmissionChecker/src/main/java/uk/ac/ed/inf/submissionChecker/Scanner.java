package uk.ac.ed.inf.submissionChecker;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Scanner {

    /**
     * pattern for the report file
     */
    public static String ReportFileName = "report_%s.html";
    public static String baseDirectory = ".";
    public static String jarFileName = "target/PizzaDronz-1.0-SNAPSHOT.jar";
    public static String configFileName;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Scanner config_file base_directory ");
            System.err.println("Example: Scanner runtasks.json d:\\IlpSubmissions");
            System.exit(1);
        }

        configFileName = args[0];
        if (Paths.get(configFileName).toFile().exists() == false){
            System.err.println("The file: " + configFileName + " does not exist");
            System.exit(2);
        }

        baseDirectory = args[1];
        if (Paths.get(baseDirectory).toFile().exists() == false){
            System.err.println("The base directory: " + baseDirectory + " does not exist");
            System.exit(3);
        }

        List<Command> commandSet = new ArrayList<>();

        if (System.getenv("JAVA_HOME") == null) {
            System.err.println("JAVA_HOME is undefined");
            System.exit(2);
        }
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            commandSet.add(new Command(new String[]{"cmd.exe", "/c", "mvn clean"}, null, "mvn clean"));
            commandSet.add(new Command(new String[]{"cmd.exe", "/c", "mvn package -Dmaven.test.skip"}, null, "mvn package w/o unit tests"));
        } else {
            commandSet.add(new Command(new String[]{"sh", "mvn", "clean"}, null, "mvn clean"));
            commandSet.add(new Command(new String[]{"sh", "mvn", "package -Dmaven.test.skip"}, null, "mvn package w/o unit tests"));
        }

        List<Path> pomFileDirectories = getSubmissionDirectories(baseDirectory);

        // traverse all directories by clean and build the package -> then check for the JAR file
        for (Path pomDir : pomFileDirectories) {

            String currentDir = Paths.get(baseDirectory).resolve(pomDir).toAbsolutePath().toString();
            System.out.println("Processing: " + currentDir);


            HtmlReportWriter reportWriter = null;
            String submissionReportName = null;
            try {
                // take the first entry in the path for the directory
                var submissionDir = pomDir.getName(0);
                submissionReportName = String.format(ReportFileName, submissionDir);
                reportWriter = new HtmlReportWriter(Path.of(baseDirectory, submissionReportName), "Submission Report for " + submissionDir, "reporttemplate.html");
            } catch (IOException e) {
                System.err.println("error creating report file: " + submissionReportName);
                System.err.println(e);
                continue;
            }

            // remove the current report file

            for (var currentCommand : commandSet) {
                try {
                    if (currentCommand.conditionalOnFileExists() != null) {
                        var path = Path.of(currentDir, currentCommand.conditionalOnFileExists());
                        if (Files.exists(path) == false) {
                            System.err.format("Skipped execution as the file: %s does not exist\n", path);
                            continue;
                        }
                    }

                    var result = executeCommand(currentDir, currentCommand, reportWriter);
                    if (result != 0) {
                        String error = String.format("Error: %d while executing: %s", result, String.join(" ", currentCommand.commandsToExecute()));
                        System.err.println(error);
                        reportWriter.writeln(error);
                    } else {
                        System.out.println("Executed command: " + String.join(" ", currentCommand.commandsToExecute()));
                    }
                } catch (IOException e) {
                    System.err.println(e);
                } catch (InterruptedException e) {
                    System.err.println(e);
                }
            }

            reportWriter.appendJavaSourceFiles(currentDir, getJavaFilesInSubmissionDirectory(currentDir));

            // write the final report
            try {
                reportWriter.writeReport();
                System.out.println("Report written to: " + reportWriter.getReportFileName());
            } catch (IOException e) {
                System.err.println("Error writing the report: " + e);
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
            pomFileDirectories = stream.filter(e -> e.getFileName().toString().equalsIgnoreCase("pom.xml")).map(e -> base.relativize(e.getParent().normalize())).toList();
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

    public static int executeCommand(String currentDirectory, Command command, HtmlReportWriter reportWriter) throws IOError, InterruptedException, IOException {

        reportWriter.beginSection(command.reportHeader(), "commandOutput");

        ProcessBuilder pb = new ProcessBuilder();
        pb.command(command.commandsToExecute());
        pb.directory(new File(currentDirectory));

        Process proc = pb.start();

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

        BufferedWriter outputWriter = null;

        // Read the output from the command
        String input = null;
        while ((input = stdInput.readLine()) != null) {
            if (reportWriter != null) {
                reportWriter.writeln(input);
            } else {
                System.out.println(input);
            }
        }

        // Read any errors from the attempted command
        String error = null;
        while ((error = stdError.readLine()) != null) {
            if (reportWriter != null) {
                reportWriter.writeln(error);
            } else {
                System.out.println(error);
            }
        }

        return proc.waitFor();
    }
}


