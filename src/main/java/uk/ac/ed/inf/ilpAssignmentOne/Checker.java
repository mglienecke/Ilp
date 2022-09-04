package uk.ac.ed.inf.ilpAssignmentOne;

import uk.ac.ed.inf.ilpAssignmentChecker.JarLoader;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class Checker {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("You have to pass the JAR-filename as a parameter");
            System.exit(1);
        }

        var jarFile = args[0];
        if (new File(jarFile).exists() == false) {
            System.err.println(String.format("The file: %s does not exist or is no valid JAR file", jarFile));
            System.exit(1);
        }

        // Method method = classToLoad.getDeclaredMethod("myMethod");
        //  Object instance = classToLoad.newInstance();
        //  Object result = method.invoke(instance);

        System.out.println(String.format("Analysis of JAR-file: %s", jarFile));

        var loader = new JarLoader();
        try {
            var definedClassesInJar = loader.loadJarFile(jarFile);
            for (var classInJar : definedClassesInJar) {
                System.out.format("Classname: %s\n", classInJar.getName());

                for (var method: classInJar.getDeclaredMethods()){
                    System.out.format("\tMethod: %s\n", method.getName());
                }
            }
        } catch (Exception e) {
            System.err.format("Error processing: %s: %s", jarFile, e);
            System.exit(2);
        }

    }

}
