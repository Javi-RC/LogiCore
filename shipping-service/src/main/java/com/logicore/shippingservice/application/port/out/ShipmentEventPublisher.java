package com.logicore.shippingservice.application.port.out;

import com.logicore.common.DomainEvent;

/**
 * Outbound port for publishing shipment domain events (ShipmentCreated / ShipmentShipped).
 */
public interface ShipmentEventPublisher {

    void publish(DomainEvent<?> event);
}