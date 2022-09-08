package uk.ac.ed.inf.ilpAssignmentChecker;

import uk.ac.ed.inf.LngLat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

public class ClassUtils {

    public static Constructor getConstructor(Class loadedClass, Class[] initParams) {
        try {
            return loadedClass.getDeclaredConstructor(initParams);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public static Method getMethod(Class loadedClass, String methodName, Class[] params) {
        try {
            return loadedClass.getDeclaredMethod(methodName, params);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public static Method getMethodWithReturnType(Class loadedClass, String methodName, Class[] params, Class returnType){
        var method = ClassUtils.getMethod(loadedClass, methodName, params);
        if (method != null) {
            if (method.getReturnType().getName() != returnType.getName()){
                method = null;
            }
        }

        return method;
    }

}
