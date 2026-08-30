package com.logicore.notificationservice.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for a notification record.
 */
@Entity
@Table(name = "notifications")
public class NotificationJpaEntity {

    @Id
    private UUID id;

    @Column(name = "type", nullable = false, length = 30)
    private String type;

    @Column(name = "correlation_id", nullable = false)
    private UUID correlationId;

    @Column(name = "recipient", nullable = false, length = 255)
    private String recipient;

    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected NotificationJpaEntity() {
    }

    public NotificationJpaEntity(UUID id, String type, UUID correlationId, String recipient, String message) {
        this.id = id;
        this.type = type;
        this.correlationId = correlationId;
        this.recipient = recipient;
        this.message = message;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}