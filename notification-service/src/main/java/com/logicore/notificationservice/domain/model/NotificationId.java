package com.logicore.notificationservice.domain.model;

import java.util.UUID;

/**
 * Value object identifying a notification.
 */
public record NotificationId(UUID value) {

    public NotificationId {
        if (value == null) {
            throw new IllegalArgumentException("Notification id must not be null");
        }
    }

    public static NotificationId newId() {
        return new NotificationId(UUID.randomUUID());
    }
}