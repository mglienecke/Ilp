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
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import uk.ac.ed.inf.ilpAssignmentOne.Constants;

/**
 * perform a runtime analysis for CW1 using reflection. All required methods and implementations are checked (except the enum where a warning is issued as this has to be done manually)
 */
public class Checker extends ClassExecutionImplementationBase {

    public final String TestGroupLngLat = "LngLat checks";
    public final String TestGroupRestaurantOrder = "Restaurant Order checks";
    public final String TestGroupInCentralAreaImpl = "nCentralArea() implementation";
    public final String TestGroupDistanceToImpl = "distanceTo() implementation";

    public final String TestGroupCloseToImpl = "closeTo() implementation";
    public final String TestGroupNextPositionImpl = "nextPosition() implementation";
    public final String TestGroupGetRestaurantsImpl = "getRestaurantsFromRestServer() implementation";
    public final String TestGroupGetDeliveryCostImpl = "getDeliveryCost() implementation";

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

                boolean lngLatClassPresent = testForClassExistence(jarFileLoader, reportWriter, Constants.LngLatClassName, TestGroupLngLat, new HtmlReportWriter.TestResultInPoints("LngLat class / record", 0.5f, HtmlReportWriter.TestResultType.Success));
                boolean orderClassPresent = testForClassExistence(jarFileLoader, reportWriter, Constants.OrderClassName, TestGroupRestaurantOrder, new HtmlReportWriter.TestResultInPoints("Order class", 0.5f, HtmlReportWriter.TestResultType.Success));
                boolean restaurantClassPresent = testForClassExistence(jarFileLoader, reportWriter, Constants.RestaurantClassName, TestGroupRestaurantOrder, new HtmlReportWriter.TestResultInPoints("Restaurant class", 0.25f, HtmlReportWriter.TestResultType.Success));
                testForClassExistence(jarFileLoader, reportWriter, Constants.MenuClassName, TestGroupRestaurantOrder, new HtmlReportWriter.TestResultInPoints("Menu class", 0.25f, HtmlReportWriter.TestResultType.Success));

                loadedLngLatClass = jarFileLoader.getClass(Constants.LngLatClassName);
                if (loadedLngLatClass == null) {
                    break;
                }

                checkLngLatClassStructure(loadedLngLatClass, reportWriter);

                // test the constructor (is needed later on as well)
                var constructorTest = testClassForCondition(loadedLngLatClass, (Class x, FunctionalTestResult currentTest) -> {
                    var constructor = ClassUtils.getConstructor(x, new Class[]{double.class, double.class});
                    if (constructor != null) {
                        currentTest.appendMessage("LngLat(double, double) present");
                        reportWriter.addTestResultInPoints(TestGroupLngLat, new HtmlReportWriter.TestResultInPoints("LngLat constructor", 0.5f, HtmlReportWriter.TestResultType.Success));
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

                if (restaurantClassPresent) {
                    checkRestaurant(jarFileLoader, reportWriter);
                } else {
                    reportWriter.addFunctionalTestResult("Restaurant tests", "no tests possible as no Restaurant class is defined", false);
                }
                if (orderClassPresent) {
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
    private boolean testForClassExistence(JarLoader jar, HtmlReportWriter reportWriter, String className, String testGroup, HtmlReportWriter.TestResultInPoints resultInPoints) {
        var testResult = reportWriter.addFunctionalTestResult(className, "", false);
        var loadedClass = jar.getClass(className);
        testResult.setSuccess(loadedClass != null);
        testResult.setMessage(String.format("class: %s is %s existent", className, (testResult.isSuccess() ? "" : "NOT")));
        reportWriter.addTestResultInPoints(testGroup, testResult.isSuccess() ? resultInPoints : new HtmlReportWriter.TestResultInPoints(resultInPoints.test(), 0f, HtmlReportWriter.TestResultType.Error));
        return testResult.isSuccess();
    }

    private void checkLngLatClassStructure(Class loadedClass, HtmlReportWriter reportWriter) {
        var test = testClassForCondition(loadedClass, (Class x, FunctionalTestResult currentTest) -> {
            var field = (x.isRecord() ? x.getDeclaredField("lng") : x.getField("lng"));

            if (field != null) {
                currentTest.appendMessage("lng field present");
            }
            return field != null;
        }, reportWriter, "check for lng field (public)", "");
        reportWriter.addTestResultInPoints(TestGroupLngLat, new HtmlReportWriter.TestResultInPoints("lng field", test.isSuccess() ? 0.25f : 0f, test.isSuccess() ? HtmlReportWriter.TestResultType.Success : HtmlReportWriter.TestResultType.Error));


        test = testClassForCondition(loadedClass, (Class x, FunctionalTestResult currentTest) -> {
            var field = (x.isRecord() ? x.getDeclaredField("lat") : x.getField("lat"));
            if (field != null) {
                currentTest.appendMessage("lat field present");
            }
            return field != null;
        }, reportWriter, "check for lat field (public)", "");
        reportWriter.addTestResultInPoints(TestGroupLngLat, new HtmlReportWriter.TestResultInPoints("lat field", test.isSuccess() ? 0.25f : 0f, test.isSuccess() ? HtmlReportWriter.TestResultType.Success : HtmlReportWriter.TestResultType.Error));

        test = testClassForCondition(loadedClass, (Class x, FunctionalTestResult currentTest) -> {
            var method = ClassUtils.getMethodWithReturnType(x, "inCentralArea", null, boolean.class);
            if (method != null) {
                currentTest.appendMessage("boolean inCentralArea() present");
            }
            return method != null;
        }, reportWriter, "check for inCentralArea()", "");
        reportWriter.addTestResultInPoints(TestGroupLngLat, new HtmlReportWriter.TestResultInPoints("inCentralArea()", test.isSuccess() ? 0.5f : 0f, test.isSuccess() ? HtmlReportWriter.TestResultType.Success : HtmlReportWriter.TestResultType.Error));

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
            reportWriter.addTestResultInPoints(TestGroupDistanceToImpl, new HtmlReportWriter.TestResultInPoints("double distanceTo(LngLat distanceObject) defined", 0.5f, HtmlReportWriter.TestResultType.Success));

            if (canConstructorBeUsed) {
                var newInstance = ClassUtils.getConstructor(x, new Class[]{double.class, double.class}).newInstance(Constants.AppletonLng, Constants.AppletonLat);
                var newInstance2 = ClassUtils.getConstructor(x, new Class[]{double.class, double.class}).newInstance(Constants.TestLng, Constants.TestLat);

                boolean success = true;

                var distance = (double) method.invoke(newInstance, newInstance);
                if (distance != 0) {
                    currentTest.appendMessage("distance with self-comparison should be 0");
                    success = false;
                }

                if (success) {
                    currentTest.appendMessage("distanceTo() for same LngLat delivered 0 (correct)");
                } else {
                    currentTest.setWarning(true);
                }


                distance = (double) method.invoke(newInstance, newInstance2);
                var compDistance = distanceTo(Constants.AppletonLng, Constants.TestLng, Constants.AppletonLat, Constants.TestLat);
                if (distance != compDistance) {
                    currentTest.appendMessage(String.format("was expecting %f as distance - got %f", compDistance, distance));
                    success = false;
                }

                if (success) {
                    currentTest.appendMessage("distanceTo() delivered correct value");
                } else {
                    currentTest.setWarning(true);
                }

                reportWriter.addTestResultInPoints(TestGroupDistanceToImpl, new HtmlReportWriter.TestResultInPoints("implementation", success ? 0.5f : 0f, success ? HtmlReportWriter.TestResultType.Success : HtmlReportWriter.TestResultType.Error));
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
            reportWriter.addTestResultInPoints(TestGroupCloseToImpl, new HtmlReportWriter.TestResultInPoints("double closeTo(LngLat distanceObject) present", 0.5f, HtmlReportWriter.TestResultType.Success));

            if (canConstructorBeUsed) {
                var newInstance = ClassUtils.getConstructor(x, new Class[]{double.class, double.class}).newInstance(Constants.AppletonLng, Constants.AppletonLat);
                var newInstance2 = ClassUtils.getConstructor(x, new Class[]{double.class, double.class}).newInstance(Constants.TestLng, Constants.TestLat);

                boolean success = true;

                var isClose = (boolean) method.invoke(newInstance, newInstance);
                if (isClose == false) {
                    currentTest.appendMessage("closeTo() for the same LngLat should be close");
                    success = false;
                }

                if (success) {
                    currentTest.appendMessage("closeTo() for same LngLat is close (correct)");
                } else {
                    currentTest.setWarning(true);
                }

                isClose = (boolean) method.invoke(newInstance, newInstance2);
                if (isClose) {
                    currentTest.appendMessage("closeTo() does not check for the proper distance which is larger than 0.00015");
                    success = false;
                }

                if (success) {
                    currentTest.appendMessage("closeTo() for remote LngLat is not close (correct)");
                } else {
                    currentTest.setWarning(true);
                }

                reportWriter.addTestResultInPoints(TestGroupCloseToImpl, new HtmlReportWriter.TestResultInPoints("implementation", success ? 0.5f : 0f, success ? HtmlReportWriter.TestResultType.Success : HtmlReportWriter.TestResultType.Error));
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
            var success = false;
            Parameter singleParam = null;
            Optional<Method> method;

            do {
                method = Arrays.stream(x.getMethods()).filter(m -> m.getName().equals("nextPosition")).findFirst();
                if (method.isPresent() == false) {
                    currentTest.setMessage("no method nextPosition() (either with enum or double)  defined");
                    break;
                }
                currentTest.appendMessage("nextPosition() is present");

                if (method.get().getReturnType().getClass().equals(loadedClass.getClass()) == false) {
                    currentTest.setMessage("nextPosition() does not return LngLat");
                    break;
                }
                currentTest.appendMessage("LngLat nextPosition() is present");

                var params = method.get().getParameters();
                if (Arrays.stream(params).count() != 1) {
                    currentTest.setMessage("no method nextPosition() (either with enum or double - just a single parameter)  defined");
                    break;
                }
                currentTest.appendMessage("nextPosition() takes 1 parameter");

                singleParam = Arrays.stream(params).findFirst().get();
                if (singleParam.getType().equals(double.class) == false && singleParam.getType().isEnum() == false) {
                    currentTest.setMessage("no method nextPosition() (either with enum or double)  defined. A single param is present, but not of the proper kind");
                    break;
                }
                currentTest.appendMessage("nextPosition() takes 1 parameter which is either enum or double");
                success = true;
            } while (false);

            reportWriter.addTestResultInPoints(TestGroupNextPositionImpl, new HtmlReportWriter.TestResultInPoints("nextPosition(double / enum) present", success ? 0.5f : 0f, success ? HtmlReportWriter.TestResultType.Success : HtmlReportWriter.TestResultType.Error));

            if (singleParam != null && singleParam.getType().equals(double.class)) {
                if (canConstructorBeUsed) {
                    var newInstance = ClassUtils.getConstructor(x, new Class[]{double.class, double.class}).newInstance(Constants.AppletonLng, Constants.AppletonLat);

                    var test1Success = false;
                    var test2Success = false;

                    try {
                        do {
                            // HOVER test
                            var posAfterMove = method.get().invoke(newInstance, null);
                            if (getFieldValueFromLngLat(posAfterMove, "lat") != Constants.AppletonLat) {
                                currentTest.appendMessage("Lat after hover (null) is not correct");
                                break;
                            }

                            if (getFieldValueFromLngLat(posAfterMove, "lng") != Constants.AppletonLng) {
                                currentTest.appendMessage("Lng after hover (null) is not correct");
                                break;
                            }

                            test1Success = true;

                            posAfterMove = method.get().invoke(newInstance, 90);
                            double nextLng = Constants.AppletonLng + Constants.MoveDistance * Math.cos(90);
                            double nextLat = Constants.AppletonLat + Constants.MoveDistance * Math.sin(90);

                            if (getFieldValueFromLngLat(posAfterMove, "lat") != nextLat) {
                                currentTest.appendMessage("Lat after North (90 deg) is not correct");
                                break;
                            }
                            if (getFieldValueFromLngLat(posAfterMove, "lng") != nextLng) {
                                currentTest.appendMessage("Lng after North (90 deg) is not correct");
                                break;
                            }

                            test2Success = true;

                        } while(false);
                    } catch (Exception moveNextEx){
                        currentTest.appendMessage("nextPosition() test - exception: " + moveNextEx.getMessage());
                    }

                    if (test1Success) {
                        currentTest.appendMessage("nextPosition() with hover correct");
                    } else {
                        currentTest.setWarning(true);
                    }
                    if (test2Success) {
                        currentTest.appendMessage("nextPosition() with North(90 deg) correct");
                    } else {
                        currentTest.setWarning(true);
                    }

                    reportWriter.addTestResultInPoints(TestGroupNextPositionImpl, new HtmlReportWriter.TestResultInPoints("hover check", test1Success ? 0.75f : 0f, test1Success ? HtmlReportWriter.TestResultType.Success : HtmlReportWriter.TestResultType.Error));
                    reportWriter.addTestResultInPoints(TestGroupNextPositionImpl, new HtmlReportWriter.TestResultInPoints("North(90 deg) check", test2Success ? 0.75f : 0f, test2Success ? HtmlReportWriter.TestResultType.Success : HtmlReportWriter.TestResultType.Error));

                    currentTest.setSuccess(test1Success && test2Success);
                } else {
                    currentTest.appendMessage("dynamic tests not run as no usable constructor was present");
                }
            } else {
                if (singleParam != null) {
                    currentTest.appendMessage("WARNING: manual test needed as an enum is passed in");
                    currentTest.setWarning(true);
                }

                reportWriter.addTestResultInPoints(TestGroupNextPositionImpl, new HtmlReportWriter.TestResultInPoints("CHECK with enum manually!", 0f, singleParam != null ? HtmlReportWriter.TestResultType.Warning : HtmlReportWriter.TestResultType.Error));
            }

            return true;
        }, reportWriter, "nextPosition() checks (structure + semantics)", "");
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

        reportWriter.addTestResultInPoints(TestGroupRestaurantOrder, new HtmlReportWriter.TestResultInPoints("Order object present", 0.5f, HtmlReportWriter.TestResultType.Success));

        testClassForCondition(loadedOrderClass.get(), (Class x, FunctionalTestResult currentTest) -> {
            var result = false;

            do {
                var method = Arrays.stream(x.getMethods()).filter(m -> m.getName().equals("getDeliveryCost")).findFirst();

                do {
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

                    result = true;
                } while (false);

                reportWriter.addTestResultInPoints(TestGroupRestaurantOrder, new HtmlReportWriter.TestResultInPoints("int getDeliveryCost(Restaurant[], String...) present", result ? 0.5f : 0f, result ? HtmlReportWriter.TestResultType.Success : HtmlReportWriter.TestResultType.Error));
                if (result) {
                    currentTest.appendMessage("getDeliveryCost() 2nd parameter is varargs String");
                } else {
                    // no further checks from here
                    break;
                }

                var loadedRestaurantClass = jar.getClass(Constants.RestaurantClassName);

                Method getRestaurantsMethod = null;

                if (loadedRestaurantClass.isRecord()) {
                    getRestaurantsMethod = loadedRestaurantClass.getDeclaredMethod("getRestaurantsFromRestServer", URL.class);
                } else {
                    getRestaurantsMethod = Arrays.stream(loadedRestaurantClass.getMethods()).filter(m -> m.getName().equals("getRestaurantsFromRestServer")).findFirst().orElse(null);
                }

                reportWriter.addTestResultInPoints(TestGroupGetRestaurantsImpl, new HtmlReportWriter.TestResultInPoints("getRestaurantsFromRestServer() present", getRestaurantsMethod != null ? 0.5f : 0f, getRestaurantsMethod != null ? HtmlReportWriter.TestResultType.Success : HtmlReportWriter.TestResultType.Error));

                Object orderInstance = null;
                Object restaurantResult = null;
                boolean test1Success = false;
                boolean test2Success = false;
                boolean test3Success = false;

                try {
                    if (getRestaurantsMethod != null){
                        getRestaurantsMethod.setAccessible(true);
                        restaurantResult = getRestaurantsMethod.invoke(loadedRestaurantClass, new URL("https://ilp-rest.azurewebsites.net"));
                        currentTest.appendMessage("instantiated Restaurant and loaded the Restaurant[]");
                    }

                    orderInstance = x.getConstructor().newInstance();
                    currentTest.appendMessage("instantiated Order");

                    try {
                        method.get().invoke(orderInstance, restaurantResult, new String[]{"Pizza1", "Pizza2"});
                        currentTest.setMessage("invalid pizza combination 1 not found");
                    } catch (InvocationTargetException t) {
                        // this is supposed to happen...
                        currentTest.appendMessage("Order - invalid Pizza combination 1 detected");
                        test1Success = true;
                    }


                    try {
                        method.get().invoke(orderInstance, restaurantResult, new String[]{"Margarita", "Meat Lover"});
                        currentTest.setMessage("invalid pizza combination 2 not found");
                    } catch (InvocationTargetException t) {
                        // this is supposed to happen...
                        currentTest.appendMessage("Order - invalid Pizza combination 2 detected");
                        test2Success = true;
                    }

                    // this should return 1000 + 1400 + 100
                    int deliveryCost = (int) method.get().invoke(orderInstance, restaurantResult, new String[]{"Margarita", "Calzone"});
                    if (deliveryCost != 1000 + 1400 + 100) {
                        currentTest.setMessage("delivery cost combination incorrect");
                    } else {
                        test3Success = true;
                    }

                } catch (Exception orderEx) {
                    currentTest.appendMessage("Order could not be instantiated - tests might be missing");
                    currentTest.setWarning(true);

                }
                reportWriter.addTestResultInPoints(TestGroupGetRestaurantsImpl, new HtmlReportWriter.TestResultInPoints("getRestaurantsFromRestServer() implementation", restaurantResult != null ? 1.5f : 0f, restaurantResult != null ? HtmlReportWriter.TestResultType.Success : HtmlReportWriter.TestResultType.Error));
                reportWriter.addTestResultInPoints(TestGroupGetDeliveryCostImpl, new HtmlReportWriter.TestResultInPoints("Invalid Pizza combination 1 detected", test1Success ? 1f : 0f, test1Success ? HtmlReportWriter.TestResultType.Success : (orderInstance != null ? HtmlReportWriter.TestResultType.Error : HtmlReportWriter.TestResultType.Warning)));
                reportWriter.addTestResultInPoints(TestGroupGetDeliveryCostImpl, new HtmlReportWriter.TestResultInPoints("Invalid Pizza combination 2 detected", test2Success ? 1f : 0f, test2Success ? HtmlReportWriter.TestResultType.Success : (orderInstance != null ? HtmlReportWriter.TestResultType.Error : HtmlReportWriter.TestResultType.Warning)));
                reportWriter.addTestResultInPoints(TestGroupGetDeliveryCostImpl, new HtmlReportWriter.TestResultInPoints("getDeliveryCost() calculated", test3Success ? 1f : 0f, test3Success ? HtmlReportWriter.TestResultType.Success : (orderInstance != null ? HtmlReportWriter.TestResultType.Error : HtmlReportWriter.TestResultType.Warning)));

                currentTest.appendMessage("Order - delivery cost correctly calculated");
                result = test1Success && test2Success && test3Success;
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

            reportWriter.addTestResultInPoints(TestGroupInCentralAreaImpl, new HtmlReportWriter.TestResultInPoints("AT check", centralAreaResult ? 1f : 0f, centralAreaResult ? HtmlReportWriter.TestResultType.Success : HtmlReportWriter.TestResultType.Error));

            if (centralAreaResult == false) {
                currentTest.appendMessage("Appleton Tower not (!) in central area");
                currentTest.setWarning(true);
            } else {
                currentTest.appendMessage("Appleton Tower correctly checked as central");
            }

            newInstance = ClassUtils.getConstructor(loadedClass, new Class[]{double.class, double.class}).newInstance(Constants.TestLng, Constants.TestLat);
            centralAreaResult = (boolean) centralAreaMethod.invoke(newInstance);

            reportWriter.addTestResultInPoints(TestGroupInCentralAreaImpl, new HtmlReportWriter.TestResultInPoints("FAR FAR AWAY check", centralAreaResult == false ? 1f : 0f, centralAreaResult == false ? HtmlReportWriter.TestResultType.Success : HtmlReportWriter.TestResultType.Error));

            if (centralAreaResult) {
                currentTest.appendMessage("FAR FAR AWAY  in (!) central area");
                currentTest.setWarning(true);
            } else {
                currentTest.appendMessage("FAR FAR AWAY correctly checked as not in central area");
            }
            return true;
        }, reportWriter, "LngLat.inCentralArea() checks", "");
    }
}
