package com.logicore.shippingservice.application.command;

import com.logicore.shippingservice.domain.model.ShipmentId;

/**
 * Command to advance a shipment to the delivered state.
 */
public record MarkDeliveredCommand(ShipmentId shipmentId) {

    public MarkDeliveredCommand {
        if (shipmentId == null) {
            throw new IllegalArgumentException("shipmentId must not be null");
        }
    }
}