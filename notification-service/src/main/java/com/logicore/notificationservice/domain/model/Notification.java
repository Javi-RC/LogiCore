package com.logicore.notificationservice.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A notification record produced from an incoming order/shipment event. Immutable.
 */
public class Notification {

    private final NotificationId id;
    private final NotificationType type;
    private final UUID correlationId;
    private final String recipient;
    private final String message;
    private final Instant createdAt;

    private Notification(NotificationId id, NotificationType type, UUID correlationId,
                         String recipient, String message, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.correlationId = Objects.requireNonNull(correlationId, "correlationId must not be null");
        if (recipient == null || recipient.isBlank()) {
            throw new IllegalArgumentException("recipient must not be blank");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
        this.recipient = recipient;
        this.message = message;
        this.createdAt = createdAt;
    }

    public static Notification create(NotificationType type, UUID correlationId,
                                      String recipient, String message) {
        return new Notification(NotificationId.newId(), type, correlationId, recipient, message, Instant.now());
    }

    public static Notification rehydrate(NotificationId id, NotificationType type, UUID correlationId,
                                         String recipient, String message, Instant createdAt) {
        return new Notification(id, type, correlationId, recipient, message, createdAt);
    }

    public NotificationId id() {
        return id;
    }

    public NotificationType type() {
        return type;
    }

    public UUID correlationId() {
        return correlationId;
    }

    public String recipient() {
        return recipient;
    }

    public String message() {
        return message;
    }

    public Instant createdAt() {
        return createdAt;
    }
}