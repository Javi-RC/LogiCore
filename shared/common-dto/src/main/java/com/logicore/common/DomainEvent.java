package com.logicore.common;

import java.time.Instant;
import java.util.UUID;

/**
 * Base envelope for all domain/integration events exchanged between services via Kafka.
 *
 * <p>Every event carries an {@code eventId} (unique per emission), an explicit
 * {@code eventType}, the time it occurred, and a {@code correlationId} used to trace
 * an end-to-end business operation across multiple services (observability).</p>
 *
 * <p>This class is intentionally shared across microservices because the event
 * contract must be agreed upon by producers and consumers. It contains <b>no</b>
 * persistence entities and <b>no</b> business logic — only the wire format.</p>
 */
public record DomainEvent<T>(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        UUID correlationId,
        T payload
) {

    public static <T> DomainEvent<T> of(String eventType, UUID correlationId, T payload) {
        return new DomainEvent<>(UUID.randomUUID(), eventType, Instant.now(), correlationId, payload);
    }
}
