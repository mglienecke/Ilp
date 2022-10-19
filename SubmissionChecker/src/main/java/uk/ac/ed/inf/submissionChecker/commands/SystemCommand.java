package uk.ac.ed.inf.submissionChecker.commands;

import uk.ac.ed.inf.submissionChecker.HtmlReportWriter;

import java.io.*;

/**
 * a simple shell command to execute (either bash / sh or CMD)
 */
public class SystemCommand extends BaseCommand {
    public CommandType getCommandType() {
        return CommandType.SystemCommandExecution;
    }

    /**
     * create the command including the shell command as such (so no sh or cmd.exe)
     * @param commandsToExecute would describe the command parameters. I.e. cmd /c mvn clean for a Maven build under Windows or sh mvn clean for macOS / Linux
     * @param dependentOnFiles any files the command is dependent upon
     * @param reportHeader the header to show
     */
    public SystemCommand (String[] commandsToExecute, String[] dependentOnFiles, String reportHeader) {
        super(CommandType.SystemCommandExecution, commandsToExecute, dependentOnFiles, reportHeader);
    }

    /**
     * create the actual command for a ProcessBuilder and execute it. All stdin and stderr is captured and written to the HtmlReportWriter
     * @param currentDirectory the current directory where execution is
     * @param reportWriter the reporting engine where output shall be written to
     * @return the exit code of the command
     */
    public int execute(String currentDirectory, HtmlReportWriter reportWriter)  {
        reportWriter.beginSection(getReportHeader(), "commandOutput");

        ProcessBuilder pb = new ProcessBuilder();
        pb.command(getCommandsToExecute());
        pb.directory(new File(currentDirectory));

        Process proc = null;
        try {
            proc = pb.start();

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

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e){
            throw new RuntimeException(e);
        }
        finally {
            reportWriter.endSection();
        }
    }
}
