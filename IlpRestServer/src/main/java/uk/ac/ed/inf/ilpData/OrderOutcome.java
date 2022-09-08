package uk.ac.ed.inf.ilpData;

public enum OrderOutcome {
    Delivered,
    InvalidCardNumber,
    InvalidExpiryDate,
    InvalidCrc,
    InvalidTotal,
    InvalidPizzaNotDefined,
    InvalidPizzaCount,
    InvalidPizzaCombinationMultipleSuppliers,
    Invalid
}
