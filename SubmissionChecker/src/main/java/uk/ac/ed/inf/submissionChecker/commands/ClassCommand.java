package uk.ac.ed.inf.submissionChecker.commands;

import uk.ac.ed.inf.submissionChecker.HtmlReportWriter;
import uk.ac.ed.inf.submissionChecker.tools.JarLoader;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;

public class ClassCommand extends BaseCommand {
    public CommandType getCommandType() {
        return CommandType.ClassExecution;
    }

    public String jarFileName;

    public ClassCommand(String classToExecute, String[] dependentOnFiles, String jarFileName) {
        super(CommandType.ClassExecution, classToExecute, dependentOnFiles);
        this.jarFileName = jarFileName;
    }

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
            System.out.println(String.format("Analysis of JAR-file: %s", jarAbsolute));
            var result = testClass.checkImplementation(loader, reportWriter);
            System.out.println("success: " + result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
}
