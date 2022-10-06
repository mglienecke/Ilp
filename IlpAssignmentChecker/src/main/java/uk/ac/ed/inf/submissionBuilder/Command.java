package uk.ac.ed.inf.submissionBuilder;

public record Command(String[] commandsToExecute, String conditionalOnFileExists, String reportHeader) {

}
