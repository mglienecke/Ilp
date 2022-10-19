package uk.ac.ed.inf.submissionChecker.commands;
import uk.ac.ed.inf.submissionChecker.FunctionalTestResult;

/**
 * functional interface for lambda functions to test without a class context
 */
@FunctionalInterface
public interface ThrowingPredicate {
    /**
     * perform a test and write results to the current test
     * @param currentTest the current test being run for output
     * @return true if success, else false
     * @throws Exception
     */
    boolean test(FunctionalTestResult currentTest) throws Exception;
}