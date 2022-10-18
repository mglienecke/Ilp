package uk.ac.ed.inf.submissionChecker.commands;

import uk.ac.ed.inf.submissionChecker.FunctionalTestResult;

@FunctionalInterface
public interface ThrowingClassPredicate<T>  {
    boolean test(T t, FunctionalTestResult currentTest) throws Exception;
}