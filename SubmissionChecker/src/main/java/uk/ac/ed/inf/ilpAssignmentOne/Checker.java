package uk.ac.ed.inf.ilpAssignmentOne;

import uk.ac.ed.inf.submissionChecker.FunctionalTestResult;
import uk.ac.ed.inf.submissionChecker.HtmlReportWriter;
import uk.ac.ed.inf.submissionChecker.commands.IClassExecutionImplementation;
import uk.ac.ed.inf.submissionChecker.commands.ThrowingPredicate;
import uk.ac.ed.inf.submissionChecker.tools.ClassUtils;
import uk.ac.ed.inf.submissionChecker.tools.JarLoader;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Objects;


public class Checker implements IClassExecutionImplementation {

    public final double MoveDistance = 0.00015;

    public final double AppletonLng = -3.186874;
    public final double AppletonLat = 55.944494;

    public final double TestLng = -3;
    public final double TestLat = 55.81;

    /**
     * A method that returns the pythagorean distance from this LngLat object to the inputted LngLat
     *
     * @param lng1 longitude 1
     * @param lng2 longitude 2
     * @param lat1 latitude 1
     * @param lat2 latitude 2
     * @return A double that contains the pythagorean distance of the inputted point from the object
     */
    public double distanceTo(double lng1, double lng2, double lat1, double lat2) {
        return Math.sqrt(Math.pow(lng1 - lng2, 2) + Math.pow(lat1 - lat2, 2));
    }

    static <C> FunctionalTestResult structuralClassTest(C c, ThrowingPredicate<C> predicate, HtmlReportWriter reportWriter, String title, String message) {
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

    public boolean checkImplementation(JarLoader jarFileLoader, HtmlReportWriter reportWriter) {

        String lngLatClassName = "uk.ac.ed.inf.LngLat";
        boolean result = false;

        do {

            try {
                Class loadedClass = null;

                // try to load the class
                var testResult = reportWriter.addFunctionalTestResult("LngLat general class check", "LngLat", false);
                loadedClass = jarFileLoader.getClass(lngLatClassName);
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

                checkDistanceTo(loadedClass, reportWriter, constructorTest.isSuccess());
                checkCloseTo(loadedClass, reportWriter, constructorTest.isSuccess());
                checkNextPosition(loadedClass, reportWriter, constructorTest.isSuccess());

                testForClassExistence(jarFileLoader, reportWriter, "uk.ac.ed.inf.Restaurant");
                testForClassExistence(jarFileLoader, reportWriter, "uk.ac.ed.inf.Order");
                testForClassExistence(jarFileLoader, reportWriter, "uk.ac.ed.inf.Menu");
                testOrder(jarFileLoader, reportWriter, "uk.ac.ed.inf.Order");
                testRestaurant(jarFileLoader, reportWriter, "uk.ac.ed.inf.Restaurant");

                if (constructorTest.isSuccess()){
                    testInCentralArea(loadedClass, reportWriter);
                } else {
                    reportWriter.addFunctionalTestResult("inCentralArea", "no check possible as no suitable class constructor can be found", false);
                }

                result = true;
            } catch (Exception e) {
                System.err.format("Error processing: %s", e);
            }

        } while (false);

        return result;
    }

    private boolean testForClassExistence(JarLoader jar, HtmlReportWriter reportWriter, String className){
        var testResult = reportWriter.addFunctionalTestResult(className, "check for class existence", false);
        var loadedClass = jar.getClass(className);
        testResult.setSuccess(loadedClass != null);
        return testResult.isSuccess();
    }

    private void checkDistanceTo(Class loadedClass, HtmlReportWriter reportWriter, boolean canConstructorBeUsed) {
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

            if (canConstructorBeUsed) {
                var newInstance = ClassUtils.getConstructor(x, new Class[]{double.class, double.class}).newInstance(AppletonLng, AppletonLat);
                var newInstance2 = ClassUtils.getConstructor(x, new Class[]{double.class, double.class}).newInstance(TestLng, TestLat);

                boolean success = true;

                var distance = (double) method.invoke(newInstance, newInstance);
                if (distance != 0) {
                    currentTest.appendMessage("distance with self-comparison should be 0");
                    success = false;
                }

                distance = (double) method.invoke(newInstance, newInstance2);
                var compDistance = distanceTo(AppletonLng, TestLng, AppletonLat, TestLat);
                if (distance != compDistance) {
                    currentTest.appendMessage(String.format("was expecting %f as distance - got %f", compDistance, distance));
                    success = false;
                }

                currentTest.setSuccess(success);
            } else {
                currentTest.appendMessage("dynamic tests not run as no constructor was present");
            }
            return true;
        }, reportWriter, "distanceTo() checks (structure + semantics)", "");
    }

    private void checkCloseTo(Class loadedClass, HtmlReportWriter reportWriter, boolean canConstructorBeUsed) {
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

            if (canConstructorBeUsed) {
                var newInstance = ClassUtils.getConstructor(x, new Class[]{double.class, double.class}).newInstance(AppletonLng, AppletonLat);
                var newInstance2 = ClassUtils.getConstructor(x, new Class[]{double.class, double.class}).newInstance(TestLng, TestLat);

                boolean success = true;

                var isClose = (boolean) method.invoke(newInstance, newInstance);
                if (isClose == false) {
                    currentTest.appendMessage("closeTo() for the same LngLat should be close");
                    success = false;
                }

                isClose = (boolean) method.invoke(newInstance, newInstance2);
                if (isClose) {
                    currentTest.appendMessage("closeTo() does not check for the proper distance which is larger than 0.00015");
                    success = false;
                }

                currentTest.setSuccess(success);
            } else {
                currentTest.appendMessage("dynamic tests not run as no constructor was present");
            }
            return true;
        }, reportWriter, "closeTo() checks - structure + semantics", "");
    }

    private void checkNextPosition(Class loadedClass, HtmlReportWriter reportWriter, boolean canConstructorBeUsed) {
        structuralClassTest(loadedClass, (Class x, FunctionalTestResult currentTest) -> {
            var method = Arrays.stream(x.getMethods()).filter(m -> m.getName().equals("nextPosition")).findFirst();
            if (method.isPresent() == false) {
                currentTest.setMessage("no method nextPosition() (either with enum or double)  defined");
                return false;
            }

            if (method.get().getReturnType().getClass().equals(loadedClass.getClass()) == false) {
                currentTest.setMessage("nextPosition() does not return LngLat");
                return false;
            }

            var params = method.get().getParameters();
            if (Arrays.stream(params).count() != 1){
                currentTest.setMessage("no method nextPosition() (either with enum or double - just a single parameter)  defined");
                return false;
            }

            var singleParam = Arrays.stream(params).findFirst().get();
            if (singleParam.getClass().equals(double.class) == false && singleParam.getType().isEnum() == false){
                currentTest.setMessage("no method nextPosition() (either with enum or double)  defined. A single param is present, but not of the proper kind");
                return false;
            }

            if (singleParam.getClass().equals(double.class)){
                if (canConstructorBeUsed) {
                    var newInstance = ClassUtils.getConstructor(x, new Class[]{double.class, double.class}).newInstance(AppletonLng, AppletonLat);

                    boolean success = true;

                    // HOVER test
                    var posAfterMove = method.get().invoke(newInstance, null);
                    if (getFieldFromLngLat(posAfterMove, "lat") != AppletonLat){
                        currentTest.appendMessage("Lat after hover (null) is not correct");
                    }
                    if (getFieldFromLngLat(posAfterMove, "lng") != AppletonLng){
                        currentTest.appendMessage("Lng after hover (null) is not correct");
                    }

                    // NORTH test
                    posAfterMove = method.get().invoke(newInstance, 90);
                    double nextLng = AppletonLng + MoveDistance * Math.cos(90);
                    double nextLat = AppletonLat + MoveDistance * Math.sin(90);

                    if (getFieldFromLngLat(posAfterMove, "lat") != nextLat){
                        currentTest.appendMessage("Lat after North (90 deg) is not correct");
                    }
                    if (getFieldFromLngLat(posAfterMove, "lng") != nextLng){
                        currentTest.appendMessage("Lng after North (90 deg) is not correct");
                    }


                    currentTest.setSuccess(success);
                } else {
                    currentTest.appendMessage("dynamic tests not run as no usable constructor was present");
                }
            } else {
                currentTest.appendMessage("WARNING: manual test needed as an enum is passed in");
                currentTest.setWarning(true);
            }

            return true;
        }, reportWriter, "distanceTo() checks (structure + semantics)", "");
    }

    private double getFieldFromLngLat(Object obj, String fieldName){
        try {
            var field = obj.getClass().isRecord() ? obj.getClass().getDeclaredField(fieldName) : obj.getClass().getField(fieldName);
            return (double) field.get(obj);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean testOrder(JarLoader jar, HtmlReportWriter reportWriter, String orderClassName){
        return true;
    }

    private boolean testRestaurant(JarLoader jar, HtmlReportWriter reportWriter, String restaurantClassName){
        return true;
    }

    private boolean testInCentralArea(Class loadedClass, HtmlReportWriter reportWriter){
        try {
            var newInstance = ClassUtils.getConstructor(loadedClass, new Class[]{double.class, double.class}).newInstance(AppletonLng, AppletonLat);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        return true;
    }
}
