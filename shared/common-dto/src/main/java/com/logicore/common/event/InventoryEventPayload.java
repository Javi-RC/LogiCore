package com.logicore.common.event;

import java.util.UUID;

/**
 * Payload for {@code StockReserved} / {@code StockReservationFailed} / {@code StockReleased}
 * events produced by the Inventory Service.
 */
public record InventoryEventPayload(
        UUID orderId,
        UUID productId,
        int quantity,
        boolean available
) {
}
