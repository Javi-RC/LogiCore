package com.logicore.orderservice.adapter.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for creating an order.
 */
public record CreateOrderRequest(
        @NotNull(message = "customerId is required")
        UUID customerId,

        @NotEmpty(message = "an order must have at least one item")
        List<@Valid Item> items
) {

    public record Item(
            @NotNull(message = "productId is required")
            UUID productId,

            @Min(value = 1, message = "quantity must be positive")
            int quantity
    ) {
    }
}