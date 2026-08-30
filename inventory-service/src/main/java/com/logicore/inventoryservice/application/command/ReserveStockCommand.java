package com.logicore.inventoryservice.application.command;

import com.logicore.inventoryservice.domain.model.ProductId;

/**
 * Command to reserve {@code quantity} units for an order (identified by {@code correlationId}).
 */
public record ReserveStockCommand(String correlationId, ProductId productId, int quantity) {

    public ReserveStockCommand {
        if (correlationId == null || correlationId.isBlank()) {
            throw new IllegalArgumentException("correlationId must not be blank");
        }
        if (productId == null) {
            throw new IllegalArgumentException("productId must not be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
    }
}