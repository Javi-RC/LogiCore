package com.logicore.inventoryservice.domain.exception;

/**
 * Thrown when an inventory item does not exist for a given product.
 */
public class InventoryItemNotFoundException extends RuntimeException {

    public InventoryItemNotFoundException(String message) {
        super(message);
    }
}