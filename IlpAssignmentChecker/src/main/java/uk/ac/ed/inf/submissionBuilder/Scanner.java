package uk.ac.ed.inf.submissionBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Scanner {

    public static void main(String[] args)  {
        if (args.length < 1) {
            System.err.println("Scanner maven | check");
            System.exit(1);
        }

        if (args[0].equalsIgnoreCase("maven")){
            if (System.getenv("JAVA_HOME") == null){
                System.err.println("JAVA_HOME is undefined");
                System.exit(2);
            }

            // System.out.println("PATH=" + System.getenv("PATH"));
        }

        List<String> pomFileDirectories = getSubmissionDirectories(".");

        // traverse all directories by clean and build the package -> then check for the JAR file
        for (String currentDir: pomFileDirectories) {
            System.out.println("Processing: " + currentDir);

            //
            if (args[0].equalsIgnoreCase("maven")){

                try {
                    ProcessBuilder pb = new ProcessBuilder();
                    if (System.getProperty("os.name").toLowerCase().startsWith("windows")){
                        // pb.command("cmd.exe", "/c", "mvn clean");
                        pb.command("cmd.exe", "/c", "mvn package -Dmaven.test.skip");

                    } else {
                        pb.command("sh", "mvn", "clean");
                    }

                    pb.directory(new File(currentDir));

                    /**
                     *
                    for (var env:  pb.environment().entrySet()) {
                        System.out.format("%s:%s", env.getKey(), env.getValue());
                    }
                    */
                    Process proc = pb.start();

                    /**
                    StreamGobbler outStreamGobbler = new StreamGobbler(proc.getInputStream(), System.out::println);
                    StreamGobbler errStreamGobbler = new StreamGobbler(proc.getErrorStream(), System.out::println);

                    Future<?> future = Executors.newSingleThreadExecutor().submit(outStreamGobbler);
                    Future<?> future2 = Executors.newSingleThreadExecutor().submit(errStreamGobbler);

                    int exitCode = proc.waitFor();

                    future.get(10, TimeUnit.SECONDS);
                    future2.get(10, TimeUnit.SECONDS);

                     */


                    BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                    BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

                    // Read the output from the command
                    System.out.println("Here is the standard output of the command:\n");
                    String input = null;
                    while ((input = stdInput.readLine()) != null) {
                        System.out.println(input);
                    }

                    // Read any errors from the attempted command
                    System.out.println("Here is the standard error of the command (if any):\n");
                    String error = null;
                    while ((error = stdError.readLine()) != null) {
                        System.out.println(error);
                    }

                    int exitVal = proc.waitFor();
                    System.out.println("Exited with: " + exitVal);
                } catch (IOException ex){
                    System.err.println(ex);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else{
                ;
            }
        }
    }

    /**
     * @return a list of all directories containing pom.xml which have to be built
     */
    public static List<String> getSubmissionDirectories(String basePath){
        List<String> pomFileDirectories = new ArrayList<>();

        try (Stream<Path> stream = Files.walk(Paths.get(basePath), Integer.MAX_VALUE)) {
            pomFileDirectories = stream.filter(e -> e.getFileName().toString().equalsIgnoreCase("pom.xml")).map(e -> e.toAbsolutePath().getParent().normalize().toString()).toList();
        } catch (IOException ioEx) {
            System.err.println(ioEx);
        }

        return pomFileDirectories;
    }



    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumer);
        }
    }
}


