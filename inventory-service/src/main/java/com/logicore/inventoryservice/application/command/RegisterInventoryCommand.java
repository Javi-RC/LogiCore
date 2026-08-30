package com.logicore.inventoryservice.application.command;

import com.logicore.inventoryservice.domain.model.ProductId;

/**
 * Command to register a new inventory item.
 */
public record RegisterInventoryCommand(ProductId productId, int quantity) {

    public RegisterInventoryCommand {
        if (productId == null) {
            throw new IllegalArgumentException("productId must not be null");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("quantity must not be negative");
        }
    }
}