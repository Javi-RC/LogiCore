package com.logicore.shippingservice.application.command;

import com.logicore.shippingservice.domain.model.CustomerId;
import com.logicore.shippingservice.domain.model.OrderId;

/**
 * Command to create a shipment for a confirmed order.
 */
public record CreateShipmentCommand(OrderId orderId, CustomerId customerId) {

    public CreateShipmentCommand {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null");
        }
        if (customerId == null) {
            throw new IllegalArgumentException("customerId must not be null");
        }
    }
}