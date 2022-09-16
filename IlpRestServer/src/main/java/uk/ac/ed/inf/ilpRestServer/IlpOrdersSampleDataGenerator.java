package uk.ac.ed.inf.ilpRestServer;

import com.google.gson.Gson;
import net.andreinc.mockneat.MockNeat;
import uk.ac.ed.inf.ilpData.Order;
import uk.ac.ed.inf.ilpData.OrderOutcome;
import uk.ac.ed.inf.ilpData.OrderWithOutcome;
import uk.ac.ed.inf.ilpRestServer.controller.IlpRestService;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static net.andreinc.mockneat.types.enums.CreditCardType.*;

/**
 * a sample order data generator (JSON-format)
 */
public class IlpOrdersSampleDataGenerator {

    public static void main(String[] args) throws IOException {
        System.out.println("ILP sample order data generator");

        // needed for the Pizzas and prices
        var restaurants = new IlpRestService().restaurants();


        // we use 2 years
        var startDate = LocalDate.of(2023, 1, 1);
        var endDate =  LocalDate.of(2023, 5, 31);


        // needed to create mock data
        MockNeat mock = MockNeat.threadLocal();

        // where our result will be returned
        List<Order> orderList = new ArrayList<>();

        // as long as we are before our end data
        var currentDate = startDate;
        while (currentDate.isBefore(endDate)) {

            for (var outcome : OrderOutcome.values()){

                var order = new OrderWithOutcome();


                // some default values
                order.orderNo = String.format("%08X", ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE));
                order.orderDate = String.format("%04d-%02d-%02d", currentDate.getYear(), currentDate.getMonthValue(), currentDate.getDayOfMonth());
                order.cvv = mock.cvvs().get();
                order.creditCardNumber = mock.creditCards().types(VISA_16, MASTERCARD, AMERICAN_EXPRESS).get();
                order.customer = mock.names().get();
                order.creditCardExpiry = String.format("%02d/%02d", ThreadLocalRandom.current().nextInt(1, 12), ThreadLocalRandom.current().nextInt(24, 28));

                // every order is valid by default
                order.orderOutcome = outcome;



                // get the current restaurant
                var currentRestaurant = restaurants[ThreadLocalRandom.current().nextInt(0, restaurants.length-1)];

                order.orderItems = Arrays.stream(currentRestaurant.menu).map(m -> m.name).toArray(String[]::new);
                order.priceTotalInPence = Arrays.stream(currentRestaurant.menu).map(m -> m.priceInPence).reduce(0, Integer::sum) + Order.OrderChargeInPence;

                switch (outcome){
                    case InvalidCvv -> {
                        var len = Integer.toString(ThreadLocalRandom.current().nextInt(1, 8));
                        if (len.equals("3")){
                            len = "4";
                        }

                        String formatString = "%" + len + "." + len + "s";
                        order.cvv = String.format(formatString, ThreadLocalRandom.current().nextInt(0, 99999999));
                    }
                    case InvalidCardNumber -> {
                        var len = ThreadLocalRandom.current().nextInt(1, 16);
                        String formatString = "%" + len + "." + len + "s";
                        order.creditCardNumber = String.format(formatString, ThreadLocalRandom.current().nextLong(1, 9999999999999999L));
                    }
                    case InvalidTotal -> {
                        order.priceTotalInPence +=  ThreadLocalRandom.current().nextInt(-100, 1000);
                    }

                    case InvalidExpiryDate -> {
                        order.creditCardExpiry = String.format("%02d/%02d", ThreadLocalRandom.current().nextInt(1, 20), ThreadLocalRandom.current().nextInt(2, 19));
                    }

                    case InvalidPizzaNotDefined -> {
                        ArrayList<String> items = new ArrayList<>();
                        items.addAll(Arrays.stream(order.orderItems).toList());
                        items.add("Pizza-Surprise " + new Random(10000).nextInt());
                        order.orderItems = items.toArray(order.orderItems);
                    }

                    case InvalidPizzaCount -> {
                        ArrayList<String> items = new ArrayList<>();
                        items.addAll(Arrays.stream(order.orderItems).toList());
                        items.addAll(Arrays.stream(order.orderItems).toList());
                        items.addAll(Arrays.stream(order.orderItems).toList());
                        items.addAll(Arrays.stream(order.orderItems).toList());
                        items.addAll(Arrays.stream(order.orderItems).toList());
                        order.orderItems = items.toArray(order.orderItems);

                        // take the base price (without order charge
                        order.priceTotalInPence = ((order.priceTotalInPence - Order.OrderChargeInPence)) * 5 + Order.OrderChargeInPence;
                    }

                    case InvalidPizzaCombinationMultipleSuppliers -> {
                        currentRestaurant = restaurants[ThreadLocalRandom.current().nextInt(0, restaurants.length-1)];

                        ArrayList<String> items = new ArrayList<>();
                        items.addAll(Arrays.stream(order.orderItems).toList());
                        items.add(currentRestaurant.menu[0].name);
                        order.orderItems = items.toArray(order.orderItems);
                        order.priceTotalInPence += currentRestaurant.menu[0].priceInPence;
                    }

                    case Invalid -> {
                        continue;
                    }
                }

                orderList.add(order);
            }

            // find another date
            currentDate = currentDate.plusDays(1);
        }

        var writer = new BufferedWriter(new FileWriter("orders.json"));
        writer.write(new Gson().toJson(orderList.toArray()));
        writer.flush();
    }
}
