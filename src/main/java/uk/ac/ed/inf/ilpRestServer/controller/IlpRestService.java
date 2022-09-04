package uk.ac.ed.inf.ilpRestServer.controller;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.google.gson.Gson;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.ed.inf.ilpData.*;
import uk.ac.ed.inf.ilpData.Order;
import uk.ac.ed.inf.ilpData.Supplier;

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
     * check if the order outcome provided matches the JSON definition
     * @param orderNo is the order no to be checked. If no or an invalid number is passed false is returned
     * @param outcomeToCheck the outcome which should be checked
     * @return true if the outcome is according to the JSON definition, otherwise (or if the order was not found) then not
     */
    @GetMapping("/orders/{orderNo}/isOrderOutcomeValid/{outcomeToCheck}")
    public Boolean isOrderOutcomeValid(@PathVariable String orderNo, @PathVariable OrderOutcome outcomeToCheck){
        var orders = getOrders();
        var currentOrder = Arrays.stream(orders).filter(o -> o.orderNo.equals(orderNo)).findFirst();
        return currentOrder.filter(orderWithOutcome -> orderWithOutcome.orderOutcome == outcomeToCheck).isPresent();
    }

    /**
     * returns the suppliers in the system
     *
     * @return array of suppliers
     */
    @GetMapping("/suppliers")
    public Supplier[] suppliers() {
        return new Gson().fromJson(new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("json/suppliers.json")))), Supplier[].class);
    }


    @GetMapping("/boundaries")
    public Boundary[] boundaries() {
        return new Gson().fromJson(new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("json/boundaries.json")))), Boundary[].class);

    }

    @GetMapping("/test")
    public TestItem test() {
        return new TestItem("Hello from the ILP-REST-Service");
    }
}
