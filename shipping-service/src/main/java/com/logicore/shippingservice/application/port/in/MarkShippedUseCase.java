package com.logicore.shippingservice.application.port.in;

import com.logicore.shippingservice.application.command.MarkShippedCommand;
import com.logicore.shippingservice.application.dto.ShipmentResponse;

/**
 * Inbound port: advance a shipment to shipped.
 */
public interface MarkShippedUseCase {

    ShipmentResponse markShipped(MarkShippedCommand command);
}