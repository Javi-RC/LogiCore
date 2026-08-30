package com.logicore.orderservice.application.command;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Immutable command for the {@code CreateOrder} use case.
 */
public record CreateOrderCommand(
        UUID customerId,
        List<Item> items
) {

    public record Item(
            UUID productId,
            int quantity
    ) {
    }

    public CreateOrderCommand {
        if (customerId == null) {
            throw new IllegalArgumentException("customerId must not be null");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("order must have at least one item");
        }
    }
}