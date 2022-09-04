package uk.ac.ed.inf.ilpData;


/**
 * Acts as a simple pizza supplier for the drone service
 */
public record Supplier(String name, double longitude, double latitude, Pizza[] menu) {
}
