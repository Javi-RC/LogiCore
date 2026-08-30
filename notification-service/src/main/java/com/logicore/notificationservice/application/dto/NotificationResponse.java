package com.logicore.notificationservice.application.dto;

import com.logicore.notificationservice.domain.model.Notification;
import com.logicore.notificationservice.domain.model.NotificationType;

import java.time.Instant;
import java.util.UUID;

/**
 * API view of a notification record.
 */
public record NotificationResponse(
        UUID id,
        NotificationType type,
        UUID correlationId,
        String recipient,
        String message,
        Instant createdAt
) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.id().value(),
                notification.type(),
                notification.correlationId(),
                notification.recipient(),
                notification.message(),
                notification.createdAt());
    }
}