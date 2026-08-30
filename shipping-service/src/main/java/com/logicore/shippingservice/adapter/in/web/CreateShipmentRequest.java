package com.logicore.shippingservice.adapter.in.web;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * REST request to manually create a shipment for a confirmed order.
 */
public record CreateShipmentRequest(
        @NotNull UUID orderId,
        @NotNull UUID customerId
) {
}