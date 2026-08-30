package com.logicore.orderservice.application.port.out;

import com.logicore.common.DomainEvent;

/**
 * Outbound port for publishing domain/integration events (e.g. to Kafka).
 */
public interface OrderEventPublisher {

    void publish(DomainEvent<?> event);
}