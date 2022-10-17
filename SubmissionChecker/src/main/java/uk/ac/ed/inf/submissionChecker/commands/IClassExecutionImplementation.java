package uk.ac.ed.inf.submissionChecker.commands;

import uk.ac.ed.inf.submissionChecker.HtmlReportWriter;
import uk.ac.ed.inf.submissionChecker.tools.JarLoader;

public interface IClassExecutionImplementation {
    boolean checkImplementation(JarLoader jarFileLoader, HtmlReportWriter reportWriter);
}
