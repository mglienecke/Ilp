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
    public int priceTotalInPence;

    public String[] orderItems;


    /**
     * Constructor for mapping
     */
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


    /**
     * Default constructor for deserialization
     */
    public Order() {

    }
}
