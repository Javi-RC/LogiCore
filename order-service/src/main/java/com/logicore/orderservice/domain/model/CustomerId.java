package com.logicore.orderservice.domain.model;

import java.util.UUID;

/**
 * Value object identifying the customer that placed an order.
 */
public record CustomerId(UUID value) {

    public CustomerId {
        if (value == null) {
            throw new IllegalArgumentException("Customer id must not be null");
        }
    }

    public static CustomerId of(UUID value) {
        return new CustomerId(value);
    }
}
