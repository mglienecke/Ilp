package uk.ac.ed.inf.ilpData;

/**
 * defines an order which is available in the system
 */
public class Order {
    public String orderNo;
    public String orderDate;
    public String customer;
    public String creditCardNumber;
    public String creditCardExpiry;
    public String cvc;
    private int priceTotalInPence;

    public String[] orderItems;
}
