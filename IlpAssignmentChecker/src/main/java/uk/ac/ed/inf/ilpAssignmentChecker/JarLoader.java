package uk.ac.ed.inf.ilpAssignmentChecker;

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

    public Class compileJavaAndReturnClass(String basePath, String javaFileWithoutExtension, String className) throws Exception {
        final String toolsJarFileName = "tools.jar";
        final String javaHome = System.getProperty("java.home");
        Path toolsJarFilePath = Paths.get(javaHome, "lib", toolsJarFileName);
        if (!Files.exists(toolsJarFilePath)){
            System.out.println("The tools jar file ("+toolsJarFileName+") could not be found at ("+toolsJarFilePath+").");
        }
        // Definition of the files to compile
        var sourceFile = new File(basePath, javaFileWithoutExtension + ".java");
        if (sourceFile.exists() == false){
            throw new RuntimeException(String.format("file: %s is not present", sourceFile.toURI()));
        }
        File[] files1 = { sourceFile };
// Get the compiler
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
// Get the file system manager of the compiler
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
// Create a compilation unit (files)
        Iterable<? extends JavaFileObject> compilationUnits =
                fileManager.getJavaFileObjectsFromFiles(Arrays.asList(files1));
// A feedback object (diagnostic) to get errors
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
// Compilation unit can be created and called only once
        JavaCompiler.CompilationTask task = compiler.getTask(
                null,
                fileManager,
                diagnostics,
                null,
                null,
                compilationUnits
        );
// The compile task is called
        task.call();
// Printing of any compile problems
        for (Diagnostic diagnostic : diagnostics.getDiagnostics())
            System.out.format("Error on line %d in %s%n",
                    diagnostic.getLineNumber(),
                    diagnostic.getSource());

// Close the compile resources
        fileManager.close();



        var compiledFile = new File(basePath, javaFileWithoutExtension + ".class");
        if (compiledFile.exists() == false){
            throw new RuntimeException(String.format("file: %s is not present", compiledFile.toURI()));
        }


        URLClassLoader classLoader = new URLClassLoader(new URL[]{compiledFile.toURI().toURL()});
        return classLoader.loadClass(className);
    }

    // get an arraylist of all the loaded classes in a jar file
    public ArrayList<Class> loadJarFile(String filePath) throws Exception {

        ArrayList<Class> availableClasses = new ArrayList<>();

        ArrayList<String> classNames = getClassNamesFromJar(filePath);
        File f = new File(filePath);

        URLClassLoader classLoader = new URLClassLoader(new URL[]{f.toURI().toURL()});
        for (String className : classNames) {
            try {
                Class cc = classLoader.loadClass(className);
                availableClasses.add(cc);
            } catch (ClassNotFoundException e) {
                System.err.println("Class " + className + " was not found:" + e);
            }
        }
        return availableClasses;
    }

    // Returns an arraylist of class names in a JarInputStream
    public ArrayList<String> getClassNamesFromJar(JarInputStream jarFile) throws Exception {
        ArrayList<String> classNames = new ArrayList<>();
        try {
            //JarInputStream jarFile = new JarInputStream(jarFileStream);
            JarEntry jar;

            //Iterate through the contents of the jar file
            while (true) {
                jar = jarFile.getNextJarEntry();
                if (jar == null) {
                    break;
                }
                //Pick file that has the extension of .class
                if ((jar.getName().endsWith(".class"))) {
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
    public ArrayList<String> getClassNamesFromJar(String jarPath) throws Exception {
        return getClassNamesFromJar(new JarInputStream(new FileInputStream(jarPath)));
    }


    /**
     * check if a class is present in the loaded classes by checking for the name
     * @param classArrayList the list of loaded classes
     * @param className the classname to check for
     * @return true if present, false if not
     */
    public boolean isClassPresent(ArrayList<Class> classArrayList, String className){
        return classArrayList.stream().anyMatch(c -> c.getName().equals(className));
    }

    /**
     * check if a class is present in the loaded classes by checking for the name
     * @param classArrayList the list of loaded classes
     * @param className the classname to check for
     * @return the loaded class definition or null
     */
    public Class getClass(ArrayList<Class> classArrayList, String className){
        return classArrayList.stream().filter(c -> c.getName().equals(className)).findFirst().orElse(null);
    }
}
