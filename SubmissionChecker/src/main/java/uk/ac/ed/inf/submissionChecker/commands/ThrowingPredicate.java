package uk.ac.ed.inf.submissionChecker.commands;

import uk.ac.ed.inf.submissionChecker.FunctionalTestResult;
import uk.ac.ed.inf.submissionChecker.HtmlReportWriter;
import uk.ac.ed.inf.submissionChecker.tools.JarLoader;

import java.util.function.Predicate;

@FunctionalInterface
public interface ThrowingPredicate<T>  {
    boolean test(T t, FunctionalTestResult currentTest) throws Exception;
}