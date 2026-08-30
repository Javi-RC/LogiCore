package com.logicore.productservice.application.dto;

import com.logicore.productservice.domain.model.Product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * API response DTO for a product.
 */
public record ProductResponse(
        UUID id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        boolean active,
        Instant createdAt
) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.id().value(),
                product.sku().value(),
                product.name(),
                product.description(),
                product.price().amount(),
                product.active(),
                product.createdAt()
        );
    }
}
