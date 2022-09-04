package uk.ac.ed.inf.ilpAssignmentOne;

import uk.ac.ed.inf.LngLat;
import uk.ac.ed.inf.ilpAssignmentChecker.ClassUtils;
import uk.ac.ed.inf.ilpAssignmentChecker.JarLoader;

import java.io.File;


public class Checker {

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Checker --jar | --compile  base directory JAR-file | Java-file classname");
            System.err.println("Example 1: Checker --jar /Users/michael/Ilp/ilpSampleJar/out/ilpSampleJar uk.ac.ed.inf.LngLat");
            System.err.println("Example 2: Checker --compile /Users/michael/Ilp/ilpSampleJar/src/main/java/uk/ac/ed/inf/LngLat.java uk.ac.ed.inf.LngLat");
            System.exit(1);
        }

        boolean isJarMode = args[0].toLowerCase().equals("--jar");
        String fileName = args[1];
        String className = args[2];
        File inputFile = new File(fileName);

        if (inputFile.exists() == false) {
            System.err.println(String.format("The file: %s does not exist", fileName));
            System.exit(1);
        }

        String errorMessage = "";

        do {
            var loader = new JarLoader();
            try {
                Class loadedClass = null;

                if (isJarMode){
                    System.out.println(String.format("Analysis of JAR-file: %s", fileName));
                    var definedClassesInJar = loader.loadJarFile(fileName);
                    loadedClass = loader.getClass(definedClassesInJar, className);
                } else {
                    var javaFileName = inputFile.getName().substring(0, inputFile.getName().indexOf("."));
                    System.out.format("processing %s.java in directory %s", javaFileName, inputFile.getPath());
                    loadedClass = loader.compileJavaAndReturnClass(inputFile.getParent(), javaFileName, className);
                    if (loadedClass == null){
                        errorMessage = "the class: " + className + " is not present in the JAR";
                        break;
                    }

                }


                var constructor = ClassUtils.getConstructor(loadedClass, new Class[]{double.class, double.class});
                if (constructor == null) {
                    errorMessage = "the class: " + className + " has no constructor: " + "LngLat" + " with 2 double parameters";
                    break;
                }


                var method = ClassUtils.getMethodWithReturnType(loadedClass, "inCentralArea", null, boolean.class);
                if (method == null){
                    errorMessage = "no method boolean inCentralArea() defined";
                    break;
                }

                method = ClassUtils.getMethodWithReturnType(loadedClass, "distanceTo", new Class[] { LngLat.class }, double.class);
                if (method == null){
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

        if (errorMessage.isEmpty() == false){
            System.err.println(errorMessage);
            System.exit(3);
        }

        System.out.println("all checks passed");
    }


}
