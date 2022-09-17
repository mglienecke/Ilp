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


        // we use a smaller period than 2 years as the result gets huge
        var startDate = LocalDate.of(2023, 1, 1);

        // needed to create mock data
        MockNeat mock = MockNeat.threadLocal();

        // where our result will be returned
        List<Order> orderList = new ArrayList<>();

        // iterate over each date in the range
        var currentDate = startDate;
        while (currentDate.isBefore(LocalDate.of(2023, 5, 31))) {

            // traverse over all possible order outcomes to generate one record for each outcome possible
            for (var outcome : OrderOutcome.values()){

                // create a valid order with default values
                var order = new OrderWithOutcome();
                order.orderNo = String.format("%08X", ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE));
                order.orderDate = String.format("%04d-%02d-%02d", currentDate.getYear(), currentDate.getMonthValue(), currentDate.getDayOfMonth());
                order.cvv = mock.cvvs().get();
                order.creditCardNumber = mock.creditCards().types(VISA_16, MASTERCARD, AMERICAN_EXPRESS).get();
                order.customer = mock.names().get();
                order.creditCardExpiry = String.format("%02d/%02d", ThreadLocalRandom.current().nextInt(1, 12), ThreadLocalRandom.current().nextInt(24, 28));

                // every order has the defined outcome
                order.orderOutcome = outcome;

                // get a random restaurant
                var currentRestaurant = restaurants[ThreadLocalRandom.current().nextInt(0, restaurants.length-1)];

                // and load the order items plus the price
                order.orderItems = Arrays.stream(currentRestaurant.menu).map(m -> m.name).toArray(String[]::new);
                order.priceTotalInPence = Arrays.stream(currentRestaurant.menu).map(m -> m.priceInPence).reduce(0, Integer::sum) + Order.OrderChargeInPence;

                // now modify the wrong entries according to the enum
                switch (outcome){
                    case InvalidCvv -> {
                        var len = Integer.toString(ThreadLocalRandom.current().nextInt(1, 8));

                        // 3 digit CVV would be valid
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
                        // by adding something to the order this invalidates the total -> just make sure it is not 0 (which would be correct again)
                        var add = ThreadLocalRandom.current().nextInt(-100, 1000);
                        if (add == 0){
                            add = 1;
                        }
                        order.priceTotalInPence += add;
                    }

                    case InvalidExpiryDate -> {
                        // expiry date is always in the past
                        order.creditCardExpiry = String.format("%02d/%02d", ThreadLocalRandom.current().nextInt(1, 20), ThreadLocalRandom.current().nextInt(2, 19));
                    }

                    case InvalidPizzaNotDefined -> {
                        // add a pizza which does not exist
                        ArrayList<String> items = new ArrayList<>();
                        items.addAll(Arrays.stream(order.orderItems).toList());
                        items.add("Pizza-Surprise " + new Random(10000).nextInt());
                        order.orderItems = items.toArray(order.orderItems);
                    }

                    case InvalidPizzaCount -> {
                        // just multiply the ordered pizzas (5 is always more than the valid maximum)
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
                        // mix pizzas from a different supplier
                        currentRestaurant = restaurants[ThreadLocalRandom.current().nextInt(0, restaurants.length-1)];

                        ArrayList<String> items = new ArrayList<>();
                        items.addAll(Arrays.stream(order.orderItems).toList());
                        items.add(currentRestaurant.menu[0].name);
                        order.orderItems = items.toArray(order.orderItems);
                        order.priceTotalInPence += currentRestaurant.menu[0].priceInPence;
                    }

                    case Invalid -> {
                        // this code is not used as such for results - only for initialization
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
