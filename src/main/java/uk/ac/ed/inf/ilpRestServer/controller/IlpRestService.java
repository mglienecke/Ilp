package uk.ac.ed.inf.ilpRestServer.controller;

import java.io.*;
import com.google.gson.Gson;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.ed.inf.ilpData.*;
import uk.ac.ed.inf.ilpData.Order;
import uk.ac.ed.inf.ilpData.Pizza;
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
        InputStream ordersJsonFileStream = getClass().getClassLoader().getResourceAsStream("json/orders.json");
        if (ordersJsonFileStream == null){
            throw new RuntimeException("orders.json not present");
        }
        InputStreamReader isr = new InputStreamReader(ordersJsonFileStream);
        BufferedReader br = new BufferedReader(isr);
        return new Gson().fromJson(br, Order[].class);
    }

    /**
     * returns the suppliers in the system
     *
     * @return array of suppliers
     */
    @GetMapping("/suppliers")
    public Supplier[] suppliers() {
        return new Supplier[]{
                new Supplier("M Pizza", 55.945535152517735, -3.1912869215011597, new Pizza[]{new Pizza("Pizza A", 1999), new Pizza("Pizza B", 2048)}),
                new Supplier("Other Pizza", 55.945535152517735, -3.1912869215011597, new Pizza[]{new Pizza("Pizza X", 999), new Pizza("Student Special", 300)})
        };
    }


    @GetMapping("/boundaries")
    public IlpServiceBoundary[] boundaries() {
        return new IlpServiceBoundary[]{
                new IlpServiceBoundary("Forrest Hill", -3.192473, 55.946233),
                new IlpServiceBoundary("KFC", -3.192473, 55.946233),
                new IlpServiceBoundary("Forrest Hill", -3.192473, 55.946233),
                new IlpServiceBoundary("Forrest Hill", -3.192473, 55.946233),

        };
    }

    @GetMapping("/test")
    public TestItem test() {
        return new TestItem("Hello from the ILP-REST-Service");
    }
}
