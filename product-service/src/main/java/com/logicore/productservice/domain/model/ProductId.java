package com.logicore.productservice.domain.model;

import java.util.UUID;

/**
 * Value object identifying a product.
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

    public static ProductId newId() {
        return new ProductId(UUID.randomUUID());
    }
}
