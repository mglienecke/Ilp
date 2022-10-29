package uk.ac.ed.inf.submissionChecker.commands;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * abstract implementation for a command to check submissions
 */
public abstract class BaseCommand implements ISubmissionCheckerCommand {

    private String classToExecute;
    private final String[] dependentOnFiles;
    private String reportHeader;


    private final CommandType commandType;
    private String[] commandsToExecute;

    private ArrayList<String> errorsDuringExecution = new ArrayList<>();

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

    /**
     * add a new error to the list
     * @param error
     */
    public void addError(String error) {
        errorsDuringExecution.add(error);
    }

    /**
     * get all errors which occurred during execution
     * @return an arraylist of the errors
     */
    public ArrayList<String> getErrorsDuringExecution() { return errorsDuringExecution; }

    /**
     * a class shall be executed
     * @param commandType
     * @param classToExecute is the class to be executed (has to be of type IClassExecutionImplementation)
     * @param dependentOnFiles describes any files which have to be present (i.e. a JAR, etc.)
     * @param reportHeader which report header to show
     */
    protected BaseCommand(CommandType commandType, String classToExecute, String[] dependentOnFiles, String reportHeader) {
        this.commandType = commandType;
        this.classToExecute = classToExecute;
        this.dependentOnFiles = dependentOnFiles;
        this.reportHeader = reportHeader;
    }

    /**
     * a shell script shall be executed
     * @param commandType
     * @param commandsToExecute a set of command - depending on the target OS this will be concatenated accordingly
     * @param dependentOnFiles describes any files which have to be present (i.e. a JAR, etc.)
     * @param reportHeader which report header to show
     */
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

    /**
     * get a description for the command
     * @return description
     */
    public String getCommandDescription(){
        String description;

        switch (commandType){
            case ClassExecution -> description = String.format("class execution: %s", classToExecute);
            case SystemCommandExecution -> description = String.format("command execution: %s", String.join(" ", commandsToExecute));
            default -> throw new RuntimeException("there is a command type missing");
        }

        return description;
    }
}
