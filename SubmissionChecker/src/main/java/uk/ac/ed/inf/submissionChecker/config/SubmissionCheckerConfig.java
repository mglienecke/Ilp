package uk.ac.ed.inf.submissionChecker.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.ac.ed.inf.submissionChecker.commands.ClassCommand;
import uk.ac.ed.inf.submissionChecker.commands.ISubmissionCheckerCommand;
import uk.ac.ed.inf.submissionChecker.commands.SystemCommand;

import java.util.ArrayList;
import java.util.List;

/**
 * the configuration to use for the submission checker
 */
public class SubmissionCheckerConfig {

    /**
     * the report template file
     */
    public String reportTemplateFile;

    /**
     * the pattern to be applied for the submission
     */
    @JsonProperty("submissionReportPattern")
    public String submissionReportPattern;

    /**
     * the array of commands to be executed (in this order)
     */
    @JsonProperty("commands")
    public SubmissionCheckerCommand[] commands;

    /**
     * convert the defined commands into commands which can be executed
     * @return the list of commands converted from their JSON definition
     */
    public ISubmissionCheckerCommand[] commandsToExecute(){

        List<ISubmissionCheckerCommand> commandList = new ArrayList<>();
        if (commands != null){
            for (var command: commands) {
                switch (command.commandType()){
                    case SystemCommandExecution -> commandList.add(new SystemCommand(command.commandsToExecute, command.conditionalOnFilesExist, command.reportHeader));
                    case ClassExecution -> commandList.add(new ClassCommand(command.classToExecute, command.conditionalOnFilesExist, command.jarFileName));
                }
            }
        }
        return commandList.toArray(ISubmissionCheckerCommand[]::new);
    }
}
