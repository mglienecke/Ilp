package uk.ac.ed.inf.ilpAssignmentOne;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tomcat.util.bcel.Const;
import uk.ac.ed.inf.submissionChecker.FunctionalTestResult;
import uk.ac.ed.inf.submissionChecker.HtmlReportWriter;
import uk.ac.ed.inf.submissionChecker.commands.ClassExecutionImplementationBase;
import uk.ac.ed.inf.submissionChecker.tools.ClassUtils;
import uk.ac.ed.inf.submissionChecker.tools.JarLoader;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import uk.ac.ed.inf.ilpAssignmentOne.Constants;

/**
 * perform a runtime analysis for CW1 using reflection. All required methods and implementations are checked (except the enum where a warning is issued as this has to be done manually)
 */
public class Checker extends ClassExecutionImplementationBase {
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

    /**
     * check the implementation of assignment 1 according to the specification document
     *
     * @param jarFileLoader
     * @param reportWriter
     * @return
     */
    public boolean checkImplementation(JarLoader jarFileLoader, HtmlReportWriter reportWriter) {

        boolean result = false;

        do {
            try {
                Class loadedLngLatClass = null;

                boolean lngLatClassPresent = testForClassExistence(jarFileLoader, reportWriter, Constants.LngLatClassName);
                boolean orderClassPresent = testForClassExistence(jarFileLoader, reportWriter, Constants.OrderClassName);
                boolean restaurantClassPresent = testForClassExistence(jarFileLoader, reportWriter, Constants.RestaurantClassName);
                testForClassExistence(jarFileLoader, reportWriter, Constants.MenuClassName);

                loadedLngLatClass = jarFileLoader.getClass(Constants.LngLatClassName);
                if (loadedLngLatClass == null) {
                    break;
                }

                checkLngLatClassStructure(loadedLngLatClass, reportWriter);

                // test the constructor (is needed later on as well)
                var constructorTest = testClassForCondition(loadedLngLatClass, (Class x, FunctionalTestResult currentTest) -> {
                    var constructor = ClassUtils.getConstructor(x, new Class[]{double.class, double.class});
                    if (constructor != null){
                        currentTest.appendMessage("LngLat(double, double) present");
                    }

                    return constructor != null;
                }, reportWriter, "constructor check", "LngLat(double, double)");

                // logical checks
                checkDistanceTo(loadedLngLatClass, reportWriter, constructorTest.isSuccess());
                checkCloseTo(loadedLngLatClass, reportWriter, constructorTest.isSuccess());
                checkNextPosition(loadedLngLatClass, reportWriter, constructorTest.isSuccess());


                if (constructorTest.isSuccess()) {
                    testInCentralArea(loadedLngLatClass, reportWriter);
                } else {
                    reportWriter.addFunctionalTestResult("inCentralArea", "no check possible as no suitable class constructor can be found", false);
                }

                if (restaurantClassPresent){
                    checkRestaurant(jarFileLoader, reportWriter);
                } else {
                    reportWriter.addFunctionalTestResult("Restaurant tests", "no tests possible as no Restaurant class is defined", false);
                }
                if (orderClassPresent){
                    checkOrder(jarFileLoader, reportWriter);
                } else {
                    reportWriter.addFunctionalTestResult("Order tests", "no tests possible as no Order class is defined", false);
                }

                result = true;
            } catch (Exception e) {
                System.err.format("Error processing: %s", e);
            }

        } while (false);

        return result;
    }

    /**
     * test if a class exists in the jar
     *
     * @param jar
     * @param reportWriter
     * @param className
     * @return
     */
    private boolean testForClassExistence(JarLoader jar, HtmlReportWriter reportWriter, String className) {
        var testResult = reportWriter.addFunctionalTestResult(className, "", false);
        var loadedClass = jar.getClass(className);
        testResult.setSuccess(loadedClass != null);
        testResult.setMessage(String.format("class: %s is %s existent", className, (testResult.isSuccess() ? "" : "NOT")));
        return testResult.isSuccess();
    }

    private void checkLngLatClassStructure(Class loadedClass, HtmlReportWriter reportWriter) {
        testClassForCondition(loadedClass, (Class x, FunctionalTestResult currentTest) -> {
            var field = (x.isRecord() ? x.getDeclaredField("lng") : x.getField("lng"));

            if (field != null) {
                currentTest.appendMessage("lng field present");
            }

            return field != null;
        }, reportWriter, "check for lng field (public)", "");

        testClassForCondition(loadedClass, (Class x, FunctionalTestResult currentTest) -> {
            var field = (x.isRecord() ? x.getDeclaredField("lat") : x.getField("lat"));
            if (field != null) {
                currentTest.appendMessage("lat field present");
            }

            return field != null;
        }, reportWriter, "check for lat field (public)", "");

        testClassForCondition(loadedClass, (Class x, FunctionalTestResult currentTest) -> {
            var method = ClassUtils.getMethodWithReturnType(x, "inCentralArea", null, boolean.class);
            if (method != null){
                currentTest.appendMessage("boolean inCentralArea() present");
            }

            return method != null;
        }, reportWriter, "check for inCentralArea() signature", "");
    }

    private void checkDistanceTo(Class loadedClass, HtmlReportWriter reportWriter, boolean canConstructorBeUsed) {
        testClassForCondition(loadedClass, (Class x, FunctionalTestResult currentTest) -> {
            var method = x.getDeclaredMethod("distanceTo", x);
            if (method == null) {
                currentTest.setMessage("no method double distanceTo(LngLat distanceObject) defined");
                return false;
            }
            currentTest.appendMessage("distanceTo(LngLat distanceObject) defined");

            if (method.getReturnType().equals(double.class) == false) {
                currentTest.setMessage("distanceTo(LngLat distanceObject) does not return double");
                return false;
            }
            currentTest.appendMessage("double distanceTo(LngLat distanceObject) defined");

            if (canConstructorBeUsed) {
                var newInstance = ClassUtils.getConstructor(x, new Class[]{double.class, double.class}).newInstance(Constants.AppletonLng, Constants.AppletonLat);
                var newInstance2 = ClassUtils.getConstructor(x, new Class[]{double.class, double.class}).newInstance(Constants.TestLng, Constants.TestLat);

                boolean success = true;

                var distance = (double) method.invoke(newInstance, newInstance);
                if (distance != 0) {
                    currentTest.appendMessage("distance with self-comparison should be 0");
                    success = false;
                }
                currentTest.appendMessage("distanceTo() for same LngLat delivered 0 (correct)");

                distance = (double) method.invoke(newInstance, newInstance2);
                var compDistance = distanceTo(Constants.AppletonLng, Constants.TestLng, Constants.AppletonLat, Constants.TestLat);
                if (distance != compDistance) {
                    currentTest.appendMessage(String.format("was expecting %f as distance - got %f", compDistance, distance));
                    success = false;
                }
                currentTest.appendMessage("distanceTo() delivered correct value");

                currentTest.setSuccess(success);
            } else {
                currentTest.appendMessage("dynamic tests not run as no constructor was present");
            }
            return true;
        }, reportWriter, "distanceTo() checks (structure + semantics)", "");
    }

    private void checkCloseTo(Class loadedClass, HtmlReportWriter reportWriter, boolean canConstructorBeUsed) {
        testClassForCondition(loadedClass, (Class x, FunctionalTestResult currentTest) -> {
            var method = x.getDeclaredMethod("closeTo", x);
            if (method == null) {
                currentTest.setMessage("no method double closeTo(LngLat distanceObject) defined");
                return false;
            }
            currentTest.appendMessage("method closeTo(LngLat distanceObject) present");

            if (method.getReturnType().equals(boolean.class) == false) {
                currentTest.setMessage("closeTo(LngLat distanceObject) does not return boolean");
                return false;
            }
            currentTest.appendMessage("double closeTo(LngLat distanceObject) present");

            if (canConstructorBeUsed) {
                var newInstance = ClassUtils.getConstructor(x, new Class[]{double.class, double.class}).newInstance(Constants.AppletonLng, Constants.AppletonLat);
                var newInstance2 = ClassUtils.getConstructor(x, new Class[]{double.class, double.class}).newInstance(Constants.TestLng, Constants.TestLat);

                boolean success = true;

                var isClose = (boolean) method.invoke(newInstance, newInstance);
                if (isClose == false) {
                    currentTest.appendMessage("closeTo() for the same LngLat should be close");
                    success = false;
                }
                currentTest.appendMessage("closeTo() for same LngLat is close (correct)");

                isClose = (boolean) method.invoke(newInstance, newInstance2);
                if (isClose) {
                    currentTest.appendMessage("closeTo() does not check for the proper distance which is larger than 0.00015");
                    success = false;
                }
                currentTest.appendMessage("closeTo() for remote LngLat is not close (correct)");

                currentTest.setSuccess(success);
            } else {
                currentTest.appendMessage("dynamic tests not run as no constructor was present");
            }
            return true;
        }, reportWriter, "closeTo() checks - structure + semantics", "");
    }

    private void checkNextPosition(Class loadedClass, HtmlReportWriter reportWriter, boolean canConstructorBeUsed) {
        testClassForCondition(loadedClass, (Class x, FunctionalTestResult currentTest) -> {

            /**
             * this call is different to the others as nextPosition can be in 2 different ways. Either with double, or with enum for the direction
             */
            var method = Arrays.stream(x.getMethods()).filter(m -> m.getName().equals("nextPosition")).findFirst();
            if (method.isPresent() == false) {
                currentTest.setMessage("no method nextPosition() (either with enum or double)  defined");
                return false;
            }
            currentTest.appendMessage("nextPosition() is present");

            if (method.get().getReturnType().getClass().equals(loadedClass.getClass()) == false) {
                currentTest.setMessage("nextPosition() does not return LngLat");
                return false;
            }
            currentTest.appendMessage("LngLat nextPosition() is present");

            var params = method.get().getParameters();
            if (Arrays.stream(params).count() != 1) {
                currentTest.setMessage("no method nextPosition() (either with enum or double - just a single parameter)  defined");
                return false;
            }
            currentTest.appendMessage("nextPosition() takes 1 parameter");

            var singleParam = Arrays.stream(params).findFirst().get();
            if (singleParam.getClass().equals(double.class) == false && singleParam.getType().isEnum() == false) {
                currentTest.setMessage("no method nextPosition() (either with enum or double)  defined. A single param is present, but not of the proper kind");
                return false;
            }
            currentTest.appendMessage("nextPosition() takes 1 parameter which is either enum or double");

            if (singleParam.getClass().equals(double.class)) {
                if (canConstructorBeUsed) {
                    var newInstance = ClassUtils.getConstructor(x, new Class[]{double.class, double.class}).newInstance(Constants.AppletonLng, Constants.AppletonLat);

                    boolean success = true;

                    // HOVER test
                    var posAfterMove = method.get().invoke(newInstance, null);
                    if (getFieldValueFromLngLat(posAfterMove, "lat") != Constants.AppletonLat) {
                        currentTest.appendMessage("Lat after hover (null) is not correct");
                    }

                    if (getFieldValueFromLngLat(posAfterMove, "lng") != Constants.AppletonLng) {
                        currentTest.appendMessage("Lng after hover (null) is not correct");
                    }
                    currentTest.appendMessage("nextPosition() with hover correct");

                    // NORTH test
                    posAfterMove = method.get().invoke(newInstance, 90);
                    double nextLng = Constants.AppletonLng + Constants.MoveDistance * Math.cos(90);
                    double nextLat = Constants.AppletonLat + Constants.MoveDistance * Math.sin(90);

                    if (getFieldValueFromLngLat(posAfterMove, "lat") != nextLat) {
                        currentTest.appendMessage("Lat after North (90 deg) is not correct");
                    }
                    if (getFieldValueFromLngLat(posAfterMove, "lng") != nextLng) {
                        currentTest.appendMessage("Lng after North (90 deg) is not correct");
                    }
                    currentTest.appendMessage("nextPosition() with North(90 deg) correct");

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

    private double getFieldValueFromLngLat(Object obj, String fieldName) {
        try {
            var field = obj.getClass().isRecord() ? obj.getClass().getDeclaredField(fieldName) : obj.getClass().getField(fieldName);
            return (double) field.get(obj);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Check the structure and logical functionality of the order class
     *
     * @param jar
     * @param reportWriter
     * @return
     */
    private boolean checkOrder(JarLoader jar, HtmlReportWriter reportWriter) {

        AtomicReference<Class> loadedOrderClass = new AtomicReference<>();

        // perform the general checks
        generalTestForCondition((FunctionalTestResult currentTest) -> {
            loadedOrderClass.set(jar.getClass(Constants.OrderClassName));
            currentTest.appendMessage(Constants.OrderClassName + " class loaded");
            return true;
        }, reportWriter, "Order class structural checks", "");

        // if no order is loaded get out
        if (loadedOrderClass.get() == null) {
            return false;
        }

        testClassForCondition(loadedOrderClass.get(), (Class x, FunctionalTestResult currentTest) -> {
            var result = false;

            do {
                var method = Arrays.stream(x.getMethods()).filter(m -> m.getName().equals("getDeliveryCost")).findFirst();
                if (method.isPresent() == false) {
                    currentTest.setMessage("no method getDeliveryCost() defined");
                    break;
                }
                currentTest.appendMessage("getDeliveryCost() existent");

                if (method.get().getReturnType().equals(int.class) == false) {
                    currentTest.setMessage("getDeliveryCost() does not return int");
                    break;
                }
                currentTest.appendMessage("getDeliveryCost() returns int");

                var params = method.get().getParameters();
                if (params.length < 2) {
                    currentTest.setMessage("getDeliveryCost() - not enough parameter present");
                    break;
                }
                currentTest.appendMessage("getDeliveryCost() takes 2 parameters");

                if (params[0].getType().isArray() == false || params[0].getType().getComponentType().getTypeName().equals(Constants.RestaurantClassName) == false) {
                    currentTest.setMessage("param 0 is either no array, or not of type: " + Constants.RestaurantClassName);
                    break;
                }
                currentTest.appendMessage("getDeliveryCost() 1st parameter is Restaurant[]");

                if (params[1].isVarArgs() == false || params[1].getType().getComponentType().equals(String.class) == false) {
                    currentTest.setMessage("param 1 is either no varargs, or not of type String...");
                    break;
                }
                currentTest.appendMessage("getDeliveryCost() 2nd parameter is varargs String");

                var loadedRestaurantClass = jar.getClass(Constants.RestaurantClassName);
                var getRestaurantsMethod = Arrays.stream(loadedRestaurantClass.getMethods()).filter(m -> m.getName().equals("getRestaurantsFromRestServer")).findFirst();
                var restaurantResult = getRestaurantsMethod.get().invoke(loadedRestaurantClass, new URL("https://ilp-rest.azurewebsites.net"));
                currentTest.appendMessage("instantiated Restaurant and loaded the Restaurant[]");

                var orderInstance = x.getConstructor().newInstance();
                currentTest.appendMessage("instantiated Order");

                try {
                    method.get().invoke(orderInstance, restaurantResult, new String[]{"Pizza1", "Pizza2"});
                    currentTest.setMessage("invalid pizza combination 1 not found");
                    break;
                } catch (InvocationTargetException t) {
                    // this is supposed to happen...
                    currentTest.appendMessage("Order - invalid Pizza combination 1 detected");
                }

                try {
                    method.get().invoke(orderInstance, restaurantResult, new String[]{"Margarita", "Meat Lover"});
                    currentTest.setMessage("invalid pizza combination 2 not found");
                    break;
                } catch (InvocationTargetException t) {
                    // this is supposed to happen...
                    currentTest.appendMessage("Order - invalid Pizza combination 2 detected");
                }

                // this should return 1000 + 1400 + 100
                int deliveryCost = (int) method.get().invoke(orderInstance, restaurantResult, new String[]{"Margarita", "Calzone"});
                if (deliveryCost != 1000 + 1400 + 100) {
                    currentTest.setMessage("delivery cost combination incorrect");
                    break;
                }
                currentTest.appendMessage("Order - delivery cost correctly calculated");

                result = true;
            } while (false);

            return result;
        }, reportWriter, "Order class methods checks", "");

        return true;
    }

    private boolean checkRestaurant(JarLoader jar, HtmlReportWriter reportWriter) {

        AtomicReference<Class> loadedRestaurantClass = new AtomicReference<>();

        // perform the general checks
        generalTestForCondition((FunctionalTestResult currentTest) -> {

            loadedRestaurantClass.set(jar.getClass(Constants.RestaurantClassName));
            currentTest.appendMessage("Restaurant class loaded");
            return true;
        }, reportWriter, "Restaurant / Menu existence checks", "check class existence and load classes");

        // if no restaurant  is loaded get out
        if (loadedRestaurantClass.get() == null) {
            return false;
        }


        testClassForCondition(loadedRestaurantClass.get(), (Class x, FunctionalTestResult currentTest) -> {
            var result = false;

            do {
                var method = Arrays.stream(x.getMethods()).filter(m -> m.getName().equals("getRestaurantsFromRestServer")).findFirst();
                if (method.isPresent() == false) {
                    currentTest.setMessage("no method getRestaurantsFromRestServer() defined");
                    break;
                }
                currentTest.appendMessage("getRestaurantsFromRestServer() defined");

                if (method.get().getReturnType().isArray() == false || method.get().getReturnType().getComponentType().equals(loadedRestaurantClass.get()) == false) {
                    currentTest.setMessage("getRestaurantsFromRestServer() does not return Restaurant[]");
                    break;
                }
                currentTest.appendMessage("getRestaurantsFromRestServer() returns Restaurant[]");

                var params = method.get().getParameters();
                if (params.length != 1 && params[0].getType().equals(URL.class) == false) {
                    currentTest.setMessage("getRestaurantsFromRestServer() does not take an URL parameter");
                    break;
                }
                currentTest.appendMessage("getRestaurantsFromRestServer() takes a URL");

                var restaurantResult = method.get().invoke(loadedRestaurantClass.get(), new URL("https://ilp-rest.azurewebsites.net"));
                if (restaurantResult == null) {
                    currentTest.setMessage("getRestaurantsFromRestServer() does not take an URL parameter");
                    break;
                }
                currentTest.appendMessage("getRestaurantsFromRestServer() returns a JSON result");

                if (Array.getLength(restaurantResult) != 4) {
                    currentTest.setMessage("getRestaurantsFromRestServer() should deliver 4 restaurants");
                    break;
                }
                currentTest.appendMessage("getRestaurantsFromRestServer() returns 4 Restaurant entries in array");


                currentTest.appendMessage("getRestaurantsFromRestServer() return as JSON:");
                ObjectMapper mapper = new ObjectMapper();
                try {
                    currentTest.appendCodeBlockMessage(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(restaurantResult), "language-json");
                } catch (JsonProcessingException e) {
                    currentTest.setMessage(e.toString());
                    break;
                }

                result = true;
            } while (false);

            return result;
        }, reportWriter, "Restaurant class methods checks", "");


        return true;
    }

    /**
     * @param loadedClass
     * @param reportWriter
     * @return
     */
    private void testInCentralArea(Class loadedClass, HtmlReportWriter reportWriter) {
        generalTestForCondition((FunctionalTestResult currentTest) -> {

            var newInstance = ClassUtils.getConstructor(loadedClass, new Class[]{double.class, double.class}).newInstance(Constants.AppletonLng, Constants.AppletonLat);
            var centralAreaMethod = loadedClass.getMethod("inCentralArea");
            var centralAreaResult = (boolean) centralAreaMethod.invoke(newInstance);

            if (centralAreaResult == false){
                currentTest.appendMessage("Appleton Tower not (!) in central area");
                currentTest.setWarning(true);
            } else {
                currentTest.appendMessage("Appleton Tower correctly checked as central");
            }

            newInstance = ClassUtils.getConstructor(loadedClass, new Class[]{double.class, double.class}).newInstance(Constants.TestLng, Constants.TestLat);
            centralAreaResult = (boolean) centralAreaMethod.invoke(newInstance);
            if (centralAreaResult){
                currentTest.appendMessage("FAR FAR AWAY  in (!) central area");
                currentTest.setWarning(true);
            } else {
                currentTest.appendMessage("FAR FAR AWAY correctly checked as not in central area");
            }
            return true;
        }, reportWriter, "LngLat.inCentralArea() checks", "");
    }
}
