package uk.ac.ed.inf.submissionChecker.commands;
import uk.ac.ed.inf.submissionChecker.FunctionalTestResult;

@FunctionalInterface
public interface ThrowingPredicate<T>  {
    boolean test(FunctionalTestResult currentTest) throws Exception;
}