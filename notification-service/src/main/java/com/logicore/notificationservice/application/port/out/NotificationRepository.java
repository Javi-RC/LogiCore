package com.logicore.notificationservice.application.port.out;

import com.logicore.notificationservice.domain.model.Notification;

import java.util.List;
import java.util.UUID;

/**
 * Outbound port for persisting and loading {@link Notification} records.
 */
public interface NotificationRepository {

    Notification save(Notification notification);

    List<Notification> findAll();

    List<Notification> findByCorrelationId(UUID correlationId);
}