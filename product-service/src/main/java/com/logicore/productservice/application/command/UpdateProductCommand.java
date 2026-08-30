package com.logicore.productservice.application.command;

import com.logicore.productservice.domain.model.ProductId;

import java.math.BigDecimal;

/**
 * Immutable command object for the {@code UpdateProduct} use case.
 */
public record UpdateProductCommand(
        ProductId id,
        String name,
        String description,
        BigDecimal price
) {
}
