package com.logicore.shippingservice.domain.model;

import java.util.UUID;

/**
 * Value object identifying the order a shipment belongs to.
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
}