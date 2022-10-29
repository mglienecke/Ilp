package uk.ac.ed.inf.submissionChecker.tools;

import javax.tools.*;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class JarLoader {

    private ArrayList<Class> classesInJar  = new ArrayList<>();

    public JarLoader(String jarFileName, String patternToMatch) throws Exception {
        ArrayList<String> classNames = getClassNamesFromJar(jarFileName, patternToMatch);
        File f = new File(jarFileName);

        URLClassLoader classLoader = new URLClassLoader(new URL[]{f.toURI().toURL()});
        for (String className : classNames) {
            try {
                classesInJar.add(classLoader.loadClass(className));
            } catch (ClassNotFoundException e) {
                System.err.println("Class " + className + " was not found:" + e);
            }
        }
    }

    // Returns an arraylist of class names in a JarInputStream
    public ArrayList<String> getClassNamesFromJar(JarInputStream jarFile, String patternToMatch) throws Exception {
        ArrayList<String> classNames = new ArrayList<>();
        try {
            JarEntry jar;

            //Iterate through the contents of the jar file
            while (true) {
                jar = jarFile.getNextJarEntry();
                if (jar == null) {
                    break;
                }
                //Pick file that has the extension of .class
                if ((jar.getName().endsWith(".class") && jar.getName().contains(patternToMatch))) {
                    String className = jar.getName().replaceAll("/", "\\.");
                    String myClass = className.substring(0, className.lastIndexOf('.'));
                    classNames.add(myClass);
                }
            }
        } catch (Exception e) {
            throw new Exception("Error while getting class names from jar", e);
        }
        return classNames;
    }

    // Returns an arraylist of class names in a JarInputStream
    // Calls the above function by converting the jar path to a stream
    public ArrayList<String> getClassNamesFromJar(String jarPath, String patternToMatch) throws Exception {
        return getClassNamesFromJar(new JarInputStream(new FileInputStream(jarPath)), patternToMatch);
    }


    /**
     * check if a class is present in the loaded classes by checking for the name
     * @param className the classname to check for
     * @return true if present, false if not
     */
    public boolean isClassPresent(String className){
        return classesInJar.stream().anyMatch(c -> c.getName().equals(className));
    }

    /**
     * check if a class is present in the loaded classes by checking for the name
     * @param className the classname to check for
     * @return the loaded class definition or null
     */
    public Class getClass(String className){
        return classesInJar.stream().filter(c -> c.getName().equals(className)).findFirst().orElse(null);
    }
}
