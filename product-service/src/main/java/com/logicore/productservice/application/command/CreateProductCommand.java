package com.logicore.productservice.application.command;

import java.math.BigDecimal;

/**
 * Immutable command object for the {@code CreateProduct} use case.
 */
public record CreateProductCommand(
        String sku,
        String name,
        String description,
        BigDecimal price
) {
}
