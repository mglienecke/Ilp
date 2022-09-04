package uk.ac.ed.inf.ilpData;

/**
 * defines an order which is available in the system
 */
public class Order {

    private final String orderNo;
    private final String orderDate;
    private final String customer;
    private final String creditCardNumber;
    private final String creditCardExpiry;
    private final String cvc;
    private int priceTotalInPence;

    private final String[] orderItems;

    public Order(String orderNo, String orderDate, String customer, String creditCardNumber, String creditCardExpiry, String cvc, int priceTotalInPence, String[] orderItems) {
        this.orderNo = orderNo;
        this.orderDate = orderDate;
        this.customer = customer;
        this.creditCardNumber = creditCardNumber;
        this.creditCardExpiry = creditCardExpiry;
        this.cvc = cvc;
        this.priceTotalInPence = priceTotalInPence;
        this.orderItems = orderItems;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public String getCustomer() {
        return customer;
    }

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public String getCreditCardExpiry() {
        return creditCardExpiry;
    }

    public String getCvc() {
        return cvc;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public int getPriceTotalInPence() {
        return priceTotalInPence;
    }

    public String[] getOrderItems() {
        return orderItems;
    }
}
