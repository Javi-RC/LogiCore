package com.logicore.shippingservice.domain.exception;

/**
 * Thrown when a shipment cannot be found.
 */
public class ShipmentNotFoundException extends RuntimeException {

    public ShipmentNotFoundException(String message) {
        super(message);
    }
}