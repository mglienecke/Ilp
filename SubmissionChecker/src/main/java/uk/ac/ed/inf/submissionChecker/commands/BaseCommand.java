package uk.ac.ed.inf.submissionChecker.commands;

import java.nio.file.Files;
import java.nio.file.Path;

public abstract class BaseCommand implements ISubmissionCheckerCommand {

    private String classToExecute;
    private final String[] dependentOnFiles;
    private String reportHeader;


    private final CommandType commandType;
    private String[] commandsToExecute;

    public String getReportHeader() {
        return reportHeader;
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public String[] getCommandsToExecute() {
        return commandsToExecute;
    }

    public String[] getDependentOnFiles() {
        return dependentOnFiles;
    }

    public String getClassToExecute() {
        return classToExecute;
    }


    protected BaseCommand(CommandType commandType, String classToExecute, String[] dependentOnFiles) {
        this.commandType = commandType;
        this.classToExecute = classToExecute;
        this.dependentOnFiles = dependentOnFiles;
    }

    protected BaseCommand(CommandType commandType, String[] commandsToExecute, String[] dependentOnFiles, String reportHeader){
        this.commandType = commandType;
        this.commandsToExecute = commandsToExecute.clone();
        this.dependentOnFiles = dependentOnFiles;
        this.reportHeader = reportHeader;
    }


    /**
     * check if the dependent on files is satisfied
     * @param currentDirectory the directory the execution currently is in
     * @return true if the check is fine (or no dependencies defined) otherwise false
     */
    public boolean checkForDependencies(String currentDirectory){
        boolean result = true;

        if (getDependentOnFiles() != null) {
            for (var dependency: getDependentOnFiles()) {
                var path = Path.of(currentDirectory, dependency);
                if (Files.exists(path) == false) {
                    System.err.format("Skipped execution as the file: %s does not exist\n", path);
                    result = false;
                    break;
                }
            }
        }

        return result;
    }


    public String[] getDependencyFiles(){
        return dependentOnFiles;
    }
}
