package uk.ac.ed.inf.submissionChecker.commands;

import uk.ac.ed.inf.submissionChecker.HtmlReportWriter;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;

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
    int execute(String currentDirectory, HtmlReportWriter reportWriter);

    boolean checkForDependencies(String currentDirectory);

    String[] getDependencyFiles();

    String[] getCommandsToExecute();

    /**
     * get a description of the command for displaying
     * @return description
     */
    String getCommandDescription();

    /**
     * get all errors which happened
     * @return a list of errors
     */
    ArrayList<String> getErrorsDuringExecution();

    /**
     * add the error
     */
    void addError(String error);
}
