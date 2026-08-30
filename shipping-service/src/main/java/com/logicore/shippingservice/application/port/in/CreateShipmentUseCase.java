package com.logicore.shippingservice.application.port.in;

import com.logicore.shippingservice.application.command.CreateShipmentCommand;
import com.logicore.shippingservice.application.dto.ShipmentResponse;

/**
 * Inbound port: create a shipment for a confirmed order.
 */
public interface CreateShipmentUseCase {

    ShipmentResponse createShipment(CreateShipmentCommand command);
}