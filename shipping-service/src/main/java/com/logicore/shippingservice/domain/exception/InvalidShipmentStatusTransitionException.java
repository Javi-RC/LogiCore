package com.logicore.shippingservice.domain.exception;

/**
 * Thrown when a state transition (e.g. ship/deliver) is not allowed from the current status.
 */
public class InvalidShipmentStatusTransitionException extends RuntimeException {

    public InvalidShipmentStatusTransitionException(String message) {
        super(message);
    }
}