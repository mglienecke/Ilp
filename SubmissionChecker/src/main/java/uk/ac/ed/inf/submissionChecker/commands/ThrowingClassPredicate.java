package uk.ac.ed.inf.submissionChecker.commands;

import uk.ac.ed.inf.submissionChecker.FunctionalTestResult;

/**
 * functional interface for lambda functions to test without a class context
 * @param <T> the type this predicate is for
 */
@FunctionalInterface
public interface ThrowingClassPredicate<T>  {
    /**
     * perform a test and write results to the current test
     * @param t an instance of the Type T to investigate
     * @param currentTest the current test being run for output
     * @return true if success, else false
     * @throws Exception
     */
    boolean test(T t, FunctionalTestResult currentTest) throws Exception;
}