package com.logicore.inventoryservice.adapter.in.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * REST request to register new stock (or add more) for a product.
 */
public record RegisterStockRequest(
        @NotNull UUID productId,
        @NotNull @Min(0) Integer quantity
) {
}