package com.logicore.orderservice.domain.model;

import java.util.UUID;

/**
 * Value object identifying an order.
 */
public record OrderId(UUID value) {

    public OrderId {
        if (value == null) {
            throw new IllegalArgumentException("Order id must not be null");
        }
    }

    public static OrderId of(UUID value) {
        return new OrderId(value);
    }

    public static OrderId newId() {
        return new OrderId(UUID.randomUUID());
    }
}
