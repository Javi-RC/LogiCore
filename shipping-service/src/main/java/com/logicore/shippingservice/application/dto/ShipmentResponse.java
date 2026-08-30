package com.logicore.shippingservice.application.dto;

import com.logicore.shippingservice.domain.model.Shipment;
import com.logicore.shippingservice.domain.model.ShipmentStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * API view of a shipment.
 */
public record ShipmentResponse(
        UUID shipmentId,
        UUID orderId,
        UUID customerId,
        ShipmentStatus status,
        Instant createdAt,
        Instant updatedAt
) {

    public static ShipmentResponse from(Shipment shipment) {
        return new ShipmentResponse(
                shipment.id().value(),
                shipment.orderId().value(),
                shipment.customerId().value(),
                shipment.status(),
                shipment.createdAt(),
                shipment.updatedAt());
    }
}