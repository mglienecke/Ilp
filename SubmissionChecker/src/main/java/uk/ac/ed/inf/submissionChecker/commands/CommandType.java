package uk.ac.ed.inf.submissionChecker.commands;

/**
 * the type of command to be used (taskstrorun.json)
 */
public enum CommandType {
    /**
     * a system command via bash / cmd
     */
    SystemCommandExecution,

    /**
     * a Java class to be executed
     */
    ClassExecution
}
