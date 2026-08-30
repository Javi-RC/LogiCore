package com.logicore.orderservice.application.port.out;

import java.util.UUID;

/**
 * Outbound port for at-least-once idempotency: records processed event ids so a redelivered
 * Kafka message is not applied twice.
 */
public interface ProcessedEventStore {

    /**
     * Records {@code eventId} as processed if it has not been processed before.
     *
     * @return {@code true} if this call performed the first (winning) recording,
     *         {@code false} if it was already processed.
     */
    boolean markIfAbsent(UUID eventId);
}