package com.logicore.inventoryservice.domain.exception;

/**
 * Thrown when there is not enough available stock to fulfill a reservation.
 */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }
}