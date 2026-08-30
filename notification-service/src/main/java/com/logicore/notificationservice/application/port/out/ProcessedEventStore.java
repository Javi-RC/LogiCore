package com.logicore.notificationservice.application.port.out;

import java.util.UUID;

/**
 * Outbound port for at-least-once idempotency: records processed event ids so a redelivered
 * Kafka message is not applied twice.
 */
public interface ProcessedEventStore {

    boolean markIfAbsent(UUID eventId);
}