package com.logicore.notificationservice.application.command;

import com.logicore.notificationservice.domain.model.NotificationType;

import java.util.UUID;

/**
 * Command to record a notification derived from a business event.
 */
public record RecordNotificationCommand(
        NotificationType type,
        UUID correlationId,
        String recipient,
        String message
) {

    public RecordNotificationCommand {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        if (correlationId == null) {
            throw new IllegalArgumentException("correlationId must not be null");
        }
        if (recipient == null || recipient.isBlank()) {
            throw new IllegalArgumentException("recipient must not be blank");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
    }
}