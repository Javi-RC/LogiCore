package com.logicore.inventoryservice.application.command;

import com.logicore.inventoryservice.domain.model.ProductId;

/**
 * Command to release previously reserved stock (compensation).
 */
public record ReleaseStockCommand(String correlationId, ProductId productId, int quantity) {

    public ReleaseStockCommand {
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