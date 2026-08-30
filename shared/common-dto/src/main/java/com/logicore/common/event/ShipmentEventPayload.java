package com.logicore.common.event;

import java.util.UUID;

/**
 * Payload for {@code ShipmentCreated} / {@code ShipmentShipped} events produced by the
 * Shipping Service.
 */
public record ShipmentEventPayload(
        UUID shipmentId,
        UUID orderId,
        String status
) {
}
