package uk.ac.ed.inf.ilpRestServer.controller;

import com.google.gson.Gson;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.ac.ed.inf.ilpData.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * the global ILP service which provides suppliers, orders and other useful things
 */
@RestController
public class IlpRestService {

    /**
     * load the orders from the JSON file and return the deserialized version
     * @return an array of orders from the file
     */
    private OrderWithOutcome[] getOrders(){
        return new Gson().fromJson(new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("json/orders.json")))), OrderWithOutcome[].class);
    }

    /**
     * returns sample orders (some of them invalid) from a template JSON file. The order outcome is removed
     *1
     * @return an array of orders
     */
    @GetMapping("/orders")
    public Order[] orders() {
        List<Order> result = new ArrayList<>();
        for (var o  : getOrders()){
            result.add(new Order(o.orderNo, o.orderDate, o.customer, o.creditCardNumber, o.creditCardExpiry, o.cvc, o.priceTotalInPence, o.orderItems.clone()));
        }
        return result.toArray(new Order[0]);
    }


    /**
     * get the details for an order
     * @param orderNo the order to search
     * @return the order details or HTTP 404 if not found
     */
    @GetMapping("/orders/{orderNo}")
    public OrderWithOutcome orderDetails(@PathVariable String orderNo){
        var currentOrder = Arrays.stream(getOrders()).filter(o -> o.orderNo.equals(orderNo)).findFirst();
        if (currentOrder.isPresent() == false){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found");
        }
        return currentOrder.get();
    }


    /**
     * check if the order outcome provided matches the JSON definition
     * @param orderNo is the order no to be checked. If no or an invalid number is passed false is returned
     * @param outcomeToCheck the outcome which should be checked
     * @return true if the outcome is according to the JSON definition, otherwise (or if the order was not found) then not
     */
    @GetMapping("/orders/{orderNo}/isOrderOutcomeValid/{outcomeToCheck}")
    public Boolean isOrderOutcomeValid(@PathVariable String orderNo, @PathVariable OrderOutcome outcomeToCheck){
        var currentOrder = Arrays.stream(getOrders()).filter(o -> o.orderNo.equals(orderNo)).findFirst();
        return currentOrder.filter(orderWithOutcome -> orderWithOutcome.orderOutcome == outcomeToCheck).isPresent();
    }

    /**
     * get the order outcome for an order
     * @param orderNo the order to search
     * @return the order outcome or HTTP 404 if not found
     */
    @GetMapping("/orders/{orderNo}/outcome")
    public OrderOutcome orderOutcome(@PathVariable String orderNo){
        var currentOrder = Arrays.stream(getOrders()).filter(o -> o.orderNo.equals(orderNo)).findFirst();
        if (currentOrder.isPresent() == false){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found");
        }
        return currentOrder.get().orderOutcome;
    }


    /**
     * returns the restaurants in the system
     *
     * @return array of suppliers
     */
    @GetMapping("/restaurants")
    public Supplier[] restaurants() {
        return new Gson().fromJson(new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("json/restaurants.json")))), Supplier[].class);
    }


    /**
     * get the defined boundaries in the system
     * @return a vector of boundaries
     */
    @GetMapping("/centralArea")
    public Boundary[] centralArea() {
        return new Gson().fromJson(new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("json/centralarea.json")))), Boundary[].class);

    }

    /**
     * get the defined boundaries in the system
     * @return a vector of boundaries
     */
    @GetMapping("/noFlyZones")
    public NoFlyZone[] noFlyZones() {
        return new Gson().fromJson(new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("json/noflyzones.json")))), NoFlyZone[].class);

    }

    /**
     * simple test method to test the service's availability
     * @param input an optional input which will be echoed
     * @return the echo
     */
    @GetMapping(value = {"/test/{input}", "/test"})
    public TestItem test(@PathVariable(required = false) String input) {
        return new TestItem(String.format("Hello from the ILP-REST-Service. Your provided value was: %s", input == null ? "not provided" : input));
    }
}