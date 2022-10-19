package uk.ac.ed.inf.ilpAssignmentOne;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

/**
 * constants in use for CW1
 */
public class Constants {
    /**
     * max distance of 1 move
     */
    public static final double MoveDistance = 0.00015;
    public static final double AppletonLng = -3.186874;
    public static final double AppletonLat = 55.944494;

    public static final double TestLng = -3;
    public static final double TestLat = 51;

    public static final String LngLatClassName = "uk.ac.ed.inf.LngLat";
    public static final String RestaurantClassName = "uk.ac.ed.inf.Restaurant";
    public static final String MenuClassName = "uk.ac.ed.inf.Menu";
    public static final String OrderClassName = "uk.ac.ed.inf.Order";
}
