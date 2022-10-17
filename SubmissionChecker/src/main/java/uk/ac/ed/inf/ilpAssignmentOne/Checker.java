package uk.ac.ed.inf.ilpAssignmentOne;

import uk.ac.ed.inf.submissionChecker.FunctionalTestResult;
import uk.ac.ed.inf.submissionChecker.HtmlReportWriter;
import uk.ac.ed.inf.submissionChecker.commands.IClassExecutionImplementation;
import uk.ac.ed.inf.submissionChecker.commands.ThrowingPredicate;
import uk.ac.ed.inf.submissionChecker.tools.ClassUtils;
import uk.ac.ed.inf.submissionChecker.tools.JarLoader;


public class Checker implements IClassExecutionImplementation {


    public final double AppletonLng = -3.186874;
    public final double AppletonLat = 55.944494;

    public final double TestLng = -3;
    public final double TestLat = 55.81;

    /**
     * A method that returns the pythagorean distance from this LngLat object to the inputted LngLat
     * @param lng1 longitude 1
     * @param lng2 longitude 2
     * @param lat1 latitude 1
     * @param lat2 latitude 2
     * @return A double that contains the pythagorean distance of the inputted point from the object
     */
    public double distanceTo(double lng1, double lng2, double lat1, double lat2){
        return Math.sqrt(Math.pow(lng1 - lng2, 2) + Math.pow(lat1-lat2, 2));
    }

    static <C> FunctionalTestResult structuralClassTest(C c, ThrowingPredicate<C> predicate, HtmlReportWriter reportWriter, String title, String message) {
        var result = reportWriter.addFunctionalTestResult(title, message, false);
        try {
            if (predicate.test(c, result)){
                result.setSuccess(true);
            }
        } catch (Exception e) {
            result.setMessage("Exception: " + e);
        }

        return result;
    }

    public boolean checkImplementation(JarLoader jarFileLoader, HtmlReportWriter reportWriter) {

        String errorMessage = "";
        String className = "uk.ac.ed.inf.LngLat";
        boolean result = false;

        do {

            try {
                Class loadedClass = null;

                // try to load the class
                var testResult = reportWriter.addFunctionalTestResult("LngLat general class check", "LngLat", false);
                loadedClass = jarFileLoader.getClass(className);
                testResult.setSuccess(true);

                structuralClassTest(loadedClass, (Class x, FunctionalTestResult currentTest) -> {
                    return x.isRecord() ? x.getDeclaredField("lng") != null : x.getField("lng") != null;
                }, reportWriter, "lng field (public)", "");

                structuralClassTest(loadedClass, (Class x, FunctionalTestResult currentTest) -> {
                    return x.isRecord() ? x.getDeclaredField("lat") != null : x.getField("lat") != null;
                }, reportWriter, "lat field (public)", "");

                structuralClassTest(loadedClass, (Class x, FunctionalTestResult currentTest) -> {
                    return ClassUtils.getMethodWithReturnType(x, "inCentralArea", null, boolean.class) != null;
                }, reportWriter, "inCentralArea() signature check", "boolean inCentralArea() - should return bool and take no parameters");


                // test the constructor
                var constructorTest = structuralClassTest(loadedClass, (Class x, FunctionalTestResult currentTest) -> {
                    return ClassUtils.getConstructor(x, new Class[]{double.class, double.class}) != null;
                }, reportWriter, "constructor check", "LngLat(double, double)");

                structuralClassTest(loadedClass, (Class x, FunctionalTestResult currentTest) -> {
                    var method = x.getDeclaredMethod("distanceTo", x);
                    if (method == null) {
                        currentTest.setMessage("no method double distanceTo(LngLat distanceObject) defined");
                        return false;
                    }

                    if (method.getReturnType().equals(double.class) == false) {
                        currentTest.setMessage("distanceTo(LngLat distanceObject) does not return double");
                        return false;
                    }

                    if (constructorTest.isSuccess()){
                        var newInstance = ClassUtils.getConstructor(x, new Class[]{double.class, double.class}).newInstance(AppletonLng, AppletonLat);
                        var newInstance2 = ClassUtils.getConstructor(x, new Class[]{double.class, double.class}).newInstance(TestLng, TestLat);

                        boolean success = true;

                        var distance = (double) method.invoke(newInstance, newInstance);
                        if (distance != 0){
                            currentTest.appendMessage("distance with self-comparison should be 0");
                            success = false;
                        }

                        distance = (double) method.invoke(newInstance, newInstance2);
                        var compDistance = distanceTo(AppletonLng, TestLng, AppletonLat, TestLat);
                        if (distance != compDistance){
                            currentTest.appendMessage(String.format("was expecting %f as distance - got %f", compDistance, distance));
                            success = false;
                        }

                        currentTest.setSuccess(success);
                    }


                    return true;
                }, reportWriter, "distanceTo checks (structure + semantics)", "");

                structuralClassTest(loadedClass, (Class x, FunctionalTestResult currentTest) -> {
                    var method = x.getDeclaredMethod("closeTo", x);
                    if (method == null) {
                        currentTest.setMessage("no method double closeTo(LngLat distanceObject) defined");
                        return false;
                    }

                    if (method.getReturnType().equals(boolean.class) == false) {
                        currentTest.setMessage("closeTo(LngLat distanceObject) does not return boolean");
                        return false;
                    }

                    if (constructorTest.isSuccess()){
                        var newInstance = ClassUtils.getConstructor(x, new Class[]{double.class, double.class}).newInstance(AppletonLng, AppletonLat);
                        var newInstance2 = ClassUtils.getConstructor(x, new Class[]{double.class, double.class}).newInstance(TestLng, TestLat);

                        boolean success = true;

                        var distance = (double) method.invoke(newInstance, newInstance);
                        if (distance == 0){
                            currentTest.appendMessage("distance with self-comparison should be 0");
                            success = false;
                        }

                        distance = (double) method.invoke(newInstance, newInstance2);
                        var compDistance = distanceTo(AppletonLng, TestLng, AppletonLat, TestLat);
                        if (distance == compDistance){
                            currentTest.appendMessage(String.format("was expecting %f as distance - got %f", compDistance, distance));
                            success = false;
                        }

                        currentTest.setSuccess(success);
                    }


                    return true;
                }, reportWriter, "close checks - structure + semantics", "");

                result = true;
            } catch (Exception e) {
                System.err.format("Error processing: %s", e);
            }

        } while (false);

        return result;
    }
}
