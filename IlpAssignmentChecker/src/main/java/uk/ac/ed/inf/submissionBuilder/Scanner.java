package uk.ac.ed.inf.submissionBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Scanner {
    public static String BuildFileName = "maven_build.txt";

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Scanner maven | check");
            System.exit(1);
        }

        Boolean isMavenBuild = args[0].equalsIgnoreCase("maven");
        List<Command> commandSet = new ArrayList<>();

        if (isMavenBuild) {
            if (System.getenv("JAVA_HOME") == null) {
                System.err.println("JAVA_HOME is undefined");
                System.exit(2);
            }
            if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
                commandSet.add(new Command(new String[]{"cmd.exe", "/c", "mvn clean"}, null));
                commandSet.add(new Command(new String[]{"cmd.exe", "/c", "mvn package -Dmaven.test.skip"}, null));
                commandSet.add(new Command(new String[]{"cmd.exe", "/c", "dir target\\PizzaDronz-1.0-SNAPSHOT.jar"}, "target\\PizzaDronz-1.0-SNAPSHOT.jar"));

            } else {
                commandSet.add(new Command(new String[]{"sh", "mvn", "clean"}, null));
                commandSet.add(new Command(new String[]{"sh", "mvn", "package -Dmaven.test.skip"}, null));
            }
        }

        List<String> pomFileDirectories = getSubmissionDirectories(".");

        // traverse all directories by clean and build the package -> then check for the JAR file
        for (String currentDir : pomFileDirectories) {
            System.out.println("Processing: " + currentDir);

            if (isMavenBuild) {
                var reportFilePath = Path.of(currentDir, BuildFileName);
                if (Files.exists(reportFilePath)) {
                    try {
                        Files.delete(reportFilePath);
                    } catch (IOException e) {
                        System.err.println(e);
                    }
                }
            }

            for (var currentCommand : commandSet) {
                try {
                    if (currentCommand.conditionalOnFileExists() != null){
                        var path = Path.of(currentDir, currentCommand.conditionalOnFileExists());
                        if (Files.exists(path) == false){
                            System.err.format("Skipped execution as the file: %s does not exist\n", path);
                            continue;
                        }
                    }

                    var result = executeCommand(currentDir, currentCommand.commandsToExecute(), "maven_build.txt");
                    if (result != 0) {
                        System.err.format("Error: %d while executing: %s", result, String.join(" ", currentCommand.commandsToExecute()));
                    } else {
                        System.out.println("Executed command: " + String.join(" ", currentCommand.commandsToExecute()));
                    }
                } catch (IOException e) {
                    System.err.println(e);
                } catch (InterruptedException e) {
                    System.err.println(e);
                }
            }

        }
    }

    /**
     * @return a list of all directories containing pom.xml which have to be built
     */
    public static List<String> getSubmissionDirectories(String basePath) {
        List<String> pomFileDirectories = new ArrayList<>();

        try (Stream<Path> stream = Files.walk(Paths.get(basePath), Integer.MAX_VALUE)) {
            pomFileDirectories = stream.filter(e -> e.getFileName().toString().equalsIgnoreCase("pom.xml")).map(e -> e.toAbsolutePath().getParent().normalize().toString()).toList();
        } catch (IOException ioEx) {
            System.err.println(ioEx);
        }

        return pomFileDirectories;
    }


    public static int executeCommand(String currentDirectory, String[] command, String outputFileName) throws IOError, InterruptedException, IOException {
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(command);
        pb.directory(new File(currentDirectory));

        Process proc = pb.start();

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

        BufferedWriter outputWriter = null;

        if (outputFileName != null) {
            var path = Path.of(currentDirectory, outputFileName);
            outputWriter = new BufferedWriter(new FileWriter(path.toFile(), true));
        }

        // Read the output from the command
        String input = null;
        while ((input = stdInput.readLine()) != null) {
            if (outputWriter != null) {
                outputWriter.write(input);
                outputWriter.newLine();
            } else {
                System.out.println(input);
            }
        }

        // Read any errors from the attempted command
        String error = null;
        while ((error = stdError.readLine()) != null) {
            if (outputWriter != null) {
                outputWriter.write(error);
                outputWriter.newLine();
            } else {
                System.out.println(error);
            }
        }

        if (outputWriter != null) {
            outputWriter.flush();
        }

        return proc.waitFor();
    }
}


