package uk.ac.ed.inf.submissionChecker.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.ac.ed.inf.submissionChecker.commands.CommandType;

/**
 * a simple submission checker command
 */
public class SubmissionCheckerCommand {
    /**
     * the type of command to execute
     */
    @JsonProperty("type")
    public String type;

    /**
     * the type as enum value
     * @return the enum of the command
     */
    public CommandType commandType() {
        return CommandType.valueOf(type);
    }

    /**
     * which command
     */
    @JsonProperty("commandsToExecute")
    public String[] commandsToExecute;

    /**
     * which file must be present for this command to execute
     */
    @JsonProperty("conditionalOnFilesExist")
    public String[] conditionalOnFilesExist;

    /**
     * the JAR file for the Java execution check
     */
    @JsonProperty("jarFileName")
    public String jarFileName;
    /**
     * which class to execute
     */
    @JsonProperty("classToExecute")
    public String classToExecute;

    /**
     * what to display in the report header
     */
    @JsonProperty("reportHeader")
    public String reportHeader;
}
