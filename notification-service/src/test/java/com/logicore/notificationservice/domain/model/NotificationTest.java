package com.logicore.notificationservice.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationTest {

    @Test
    void createsNotificationWithDefaults() {
        Notification notification = Notification.create(
                NotificationType.ORDER_CONFIRMED, UUID.randomUUID(), "customer-1", "Your order is confirmed.");

        assertThat(notification.type()).isEqualTo(NotificationType.ORDER_CONFIRMED);
        assertThat(notification.id()).isNotNull();
        assertThat(notification.createdAt()).isNotNull();
    }

    @Test
    void rejectsBlankRecipient() {
        assertThatThrownBy(() -> Notification.create(
                NotificationType.ORDER_CREATED, UUID.randomUUID(), "  ", "message"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNullType() {
        assertThatThrownBy(() -> Notification.create(
                null, UUID.randomUUID(), "customer-1", "message"))
                .isInstanceOf(NullPointerException.class);
    }
}