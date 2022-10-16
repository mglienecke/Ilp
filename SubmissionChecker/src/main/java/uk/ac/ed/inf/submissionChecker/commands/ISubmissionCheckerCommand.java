package uk.ac.ed.inf.submissionChecker.commands;

import uk.ac.ed.inf.submissionChecker.HtmlReportWriter;

import java.io.IOError;
import java.io.IOException;

public interface ISubmissionCheckerCommand {
    /**
     * get the type
     * @return the type
     */
    CommandType getCommandType();

    /**
     * execute the command
     * @return 0 if things went ok, otherwise an error code
     * @param currentDirectory the current directory where execution is
     * @param reportWriter the reporting engine where output shall be written to
     */
    int execute(String currentDirectory, HtmlReportWriter reportWriter) throws IOError, InterruptedException, IOException;

    boolean checkForDependencies(String currentDirectory);

    String[] getDependencyFiles();

    String[] getCommandsToExecute();
}
