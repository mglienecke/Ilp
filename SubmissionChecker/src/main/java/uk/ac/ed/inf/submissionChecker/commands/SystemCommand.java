package uk.ac.ed.inf.submissionChecker.commands;

import uk.ac.ed.inf.submissionChecker.HtmlReportWriter;

import java.io.*;

public class SystemCommand extends BaseCommand {
    public CommandType getCommandType() {
        return CommandType.SystemCommandExecution;
    }

    public SystemCommand (String[] commandsToExecute, String[] dependentOnFiles, String reportHeader) {
        super(CommandType.SystemCommandExecution, commandsToExecute, dependentOnFiles, reportHeader);
    }

    public int execute(String currentDirectory, HtmlReportWriter reportWriter) throws IOError, InterruptedException, IOException {
        // LINUX / macOS
        // new String[]{"sh", "mvn", "clean"}, null, "mvn clean"));
        // new String[]{"sh", "mvn", "package -Dmaven.test.skip"}, null, "mvn package w/o unit tests"));

        reportWriter.beginSection(getReportHeader(), "commandOutput");

        ProcessBuilder pb = new ProcessBuilder();
        pb.command(getCommandsToExecute());
        pb.directory(new File(currentDirectory));

        Process proc = pb.start();

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

        // Read the output from the command
        String input;
        while ((input = stdInput.readLine()) != null) {
            if (reportWriter != null) {
                reportWriter.writeln(input);
            } else {
                System.out.println(input);
            }
        }

        // Read any errors from the attempted command
        String error;
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
