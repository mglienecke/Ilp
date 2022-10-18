package uk.ac.ed.inf.submissionChecker.commands;

import uk.ac.ed.inf.submissionChecker.FunctionalTestResult;
import uk.ac.ed.inf.submissionChecker.HtmlReportWriter;

public abstract class ClassExecutionBase implements IClassExecutionImplementation {
    /**
     * perform a test on a class using a lambda
     * @param c the class to test
     * @param predicate the lambda predicate to execute
     * @param reportWriter where output goes to
     * @param title the title of the test
     * @param message the message of the test
     * @return a functional test result for further manipulation
     * @param <C> the class this is for
     */
    public static <C> FunctionalTestResult testClassForCondition(C c, ThrowingClassPredicate<C> predicate, HtmlReportWriter reportWriter, String title, String message) {
        var result = reportWriter.addFunctionalTestResult(title, message, false);
        try {
            if (predicate.test(c, result)) {
                result.setSuccess(true);
            }
        } catch (Exception e) {
            result.setMessage("Exception: " + e);
        }

        return result;
    }

    /**
     * perform a test on a class using a lambda
     * @param predicate the lambda predicate to execute
     * @param reportWriter where output goes to
     * @param title the title of the test
     * @param message the message of the test
     * @return a functional test result for further manipulation
     * @param <C> the class this is for
     */
    public static <C> FunctionalTestResult generalTestForCondition(ThrowingPredicate<C> predicate, HtmlReportWriter reportWriter, String title, String message) {
        var result = reportWriter.addFunctionalTestResult(title, message, false);
        try {
            if (predicate.test(result)) {
                result.setSuccess(true);
            }
        } catch (Exception e) {
            result.setMessage("Exception: " + e);
        }

        return result;
    }

}
