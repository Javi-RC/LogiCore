package com.logicore.inventoryservice.adapter.in.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * REST request to reserve (or release) a quantity of stock for a product.
 * {@code correlationId} links the operation to the originating order.
 */
public record StockOperationRequest(
        @NotNull String correlationId,
        @NotNull @Min(1) Integer quantity
) {
}