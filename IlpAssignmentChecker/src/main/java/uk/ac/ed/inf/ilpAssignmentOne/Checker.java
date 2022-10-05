package uk.ac.ed.inf.ilpAssignmentOne;

import uk.ac.ed.inf.LngLat;
import uk.ac.ed.inf.ilpAssignmentChecker.ClassUtils;
import uk.ac.ed.inf.ilpAssignmentChecker.JarLoader;

import java.io.File;


public class Checker {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Checker JAR-file");
            System.err.println("Example: Checker /Users/michael/Ilp/ilpSampleJar/out/ilpSampleJar");
            System.exit(1);
        }

        String fileName = args[0];

        if (!new File(fileName).exists()) {
            System.err.println(String.format("The JAR-file: %s does not exist", fileName));
            System.exit(1);
        }

        String errorMessage = "";
        String className = "uk.ac.ed.inf.LngLat";

        do {
            var loader = new JarLoader();
            try {
                Class loadedClass = null;

                System.out.println(String.format("Analysis of JAR-file: %s", fileName));
                var definedClassesInJar = loader.loadJarFile(fileName);
                loadedClass = loader.getClass(definedClassesInJar, className);

                var constructor = ClassUtils.getConstructor(loadedClass, new Class[]{double.class, double.class});
                if (constructor == null) {
                    errorMessage = "the class: " + className + " has no constructor: " + "LngLat" + " with 2 double parameters";
                    break;
                }


                var method = ClassUtils.getMethodWithReturnType(loadedClass, "inCentralArea", null, boolean.class);
                if (method == null) {
                    errorMessage = "no method boolean inCentralArea() defined";
                    break;
                }

                method = ClassUtils.getMethodWithReturnType(loadedClass, "distanceTo", new Class[]{LngLat.class}, double.class);
                if (method == null) {
                    errorMessage = "no method double distanceTo(LngLat distanceObject) defined";
                    break;
                }


                /*
                var instance = constructor.newInstance(-3.1912869215011597, 55.945535152517735);
                if (instance == null){
                    errorMessage = "error creating a class instance";
                    break;
                }
                 */

            } catch (Exception e) {
                // System.err.format("Error processing: %s: %s", jarFile, e);
                System.err.format("Error processing: %s", e);
                System.exit(2);
            }

        } while (false);

        if (errorMessage.isEmpty() == false) {
            System.err.println(errorMessage);
            System.exit(3);
        }

        System.out.println("all checks passed");
    }


}
