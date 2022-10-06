package uk.ac.ed.inf.submissionChecker;

public record Command(String[] commandsToExecute, String conditionalOnFileExists, String reportHeader) {

}
