package com.logicore.shippingservice.application.port.in;

import com.logicore.shippingservice.application.command.MarkDeliveredCommand;
import com.logicore.shippingservice.application.dto.ShipmentResponse;

/**
 * Inbound port: advance a shipment to delivered.
 */
public interface MarkDeliveredUseCase {

    ShipmentResponse markDelivered(MarkDeliveredCommand command);
}