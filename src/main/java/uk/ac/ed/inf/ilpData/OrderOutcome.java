package uk.ac.ed.inf.ilpData;

public enum OrderOutcome {
    Delivered,
    InvalidCardNumber,
    InvalidMatricNumber,
    InvalidExpiryDate,
    CardOutOfDate,
    InvalidCrc,
    InvalidTotal,
    Invalid
}
