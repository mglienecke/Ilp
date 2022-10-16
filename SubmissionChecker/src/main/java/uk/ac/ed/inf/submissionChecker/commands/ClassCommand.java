package uk.ac.ed.inf.submissionChecker.commands;

import uk.ac.ed.inf.submissionChecker.HtmlReportWriter;

import java.io.*;

public class ClassCommand extends BaseCommand {
    public CommandType getCommandType() {
        return CommandType.ClassExecution;
    }

    public ClassCommand(String classToExecute, String[] dependentOnFiles) {
        super(CommandType.ClassExecution, classToExecute, dependentOnFiles);
    }

    public int execute(String currentDirectory, HtmlReportWriter reportWriter) throws IOError {
        return 0;
    }
}
