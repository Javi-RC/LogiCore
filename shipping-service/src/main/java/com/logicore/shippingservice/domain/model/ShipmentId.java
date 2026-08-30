package com.logicore.shippingservice.domain.model;

import java.util.UUID;

/**
 * Value object identifying a shipment.
 */
public record ShipmentId(UUID value) {

    public ShipmentId {
        if (value == null) {
            throw new IllegalArgumentException("Shipment id must not be null");
        }
    }

    public static ShipmentId of(UUID value) {
        return new ShipmentId(value);
    }

    public static ShipmentId newId() {
        return new ShipmentId(UUID.randomUUID());
    }
}