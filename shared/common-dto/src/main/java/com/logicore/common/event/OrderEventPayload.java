package com.logicore.common.event;

import java.util.List;
import java.util.UUID;

/**
 * Payload for {@code OrderCreated} / {@code OrderConfirmed} / {@code OrderCancelled} events.
 */
public record OrderEventPayload(
        UUID orderId,
        UUID customerId,
        String status,
        List<OrderItemPayload> items
) {

    public record OrderItemPayload(
            UUID productId,
            int quantity,
            String unitPrice
    ) {
    }
}
