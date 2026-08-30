package com.logicore.shippingservice.application.command;

import com.logicore.shippingservice.domain.model.ShipmentId;

/**
 * Command to advance a shipment to the shipped state.
 */
public record MarkShippedCommand(ShipmentId shipmentId) {

    public MarkShippedCommand {
        if (shipmentId == null) {
            throw new IllegalArgumentException("shipmentId must not be null");
        }
    }
}