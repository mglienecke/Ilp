package uk.ac.ed.inf.submissionChecker.commands;

import uk.ac.ed.inf.submissionChecker.HtmlReportWriter;
import uk.ac.ed.inf.submissionChecker.tools.JarLoader;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;

/**
 * run a class as a command for the SubmissionChecker.
 */
public class ClassCommand extends BaseCommand {
    public CommandType getCommandType() {
        return CommandType.ClassExecution;
    }

    public String jarFileName;

    /**
     * Create the class command
     * @param classToExecute which class (fully qualified)
     * @param dependentOnFiles which files this command is dependent upon (i.e. JAR)
     * @param jarFileName which JAR does the command investigate - usually the same as the dependent files
     * @param reportHeader which report header to create
     */
    public ClassCommand(String classToExecute, String[] dependentOnFiles, String jarFileName, String reportHeader) {
        super(CommandType.ClassExecution, classToExecute, dependentOnFiles, reportHeader);
        this.jarFileName = jarFileName;
    }

    /**
     * load the command to execute and the target JAR. Then launch the analysis of the JAR
     * @param currentDirectory the current directory where execution is
     * @param reportWriter the reporting engine where output shall be written to
     * @return
     */
    public int execute(String currentDirectory, HtmlReportWriter reportWriter) {
        IClassExecutionImplementation testClass = null;
        try {
            testClass = (IClassExecutionImplementation) ClassLoader.getSystemClassLoader().loadClass(getClassToExecute()).getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        try {
            var jarAbsolute = Paths.get(currentDirectory, jarFileName).toAbsolutePath().toString();
            var loader = new JarLoader(jarAbsolute);
            // reportWriter.beginSection(getReportHeader(), "");
            System.out.println(String.format("Analysis of JAR-file: %s", jarAbsolute));
            var result = testClass.checkImplementation(loader, reportWriter);
            System.out.println("success: " + result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            // reportWriter.endSection();
        }
        return 0;
    }
}
