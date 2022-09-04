package uk.ac.ed.inf.ilpRestServer.controller;

import java.io.*;
import com.google.gson.Gson;
import org.springframework.web.bind.annotation.GetMapping;
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
     * returns sample orders (some of them invalid) from a template JSON file in the resources by converting them to an object
     *1
     * @return an array of orders
     */
    @GetMapping("/orders")
    public Order[] orders() {
        return new Gson().fromJson(new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("json/orders.json"))), Order[].class);
    }

    /**
     * returns the suppliers in the system
     *
     * @return array of suppliers
     */
    @GetMapping("/suppliers")
    public Supplier[] suppliers() {
        return new Gson().fromJson(new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("json/suppliers.json"))), Supplier[].class);
    }


    @GetMapping("/boundaries")
    public Boundary[] boundaries() {
        return new Gson().fromJson(new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("json/boundaries.json"))), Boundary[].class);

    }

    @GetMapping("/test")
    public TestItem test() {
        return new TestItem("Hello from the ILP-REST-Service");
    }
}
