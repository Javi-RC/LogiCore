package com.logicore.inventoryservice.domain.model;

import java.util.UUID;

/**
 * Value object identifying an inventory item by product.
 */
public record ProductId(UUID value) {

    public ProductId {
        if (value == null) {
            throw new IllegalArgumentException("Product id must not be null");
        }
    }

    public static ProductId of(UUID value) {
        return new ProductId(value);
    }
}