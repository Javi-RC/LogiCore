package com.logicore.inventoryservice.application.port.out;

import com.logicore.common.DomainEvent;

/**
 * Outbound port for publishing inventory domain events (stock reserved/released) to Kafka.
 */
public interface InventoryEventPublisher {

    void publish(DomainEvent<?> event);
}