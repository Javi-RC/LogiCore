package com.logicore.notificationservice.application.port.in;

import com.logicore.notificationservice.application.dto.NotificationResponse;

import java.util.List;
import java.util.UUID;

/**
 * Inbound port: query notification history.
 */
public interface GetNotificationsUseCase {

    List<NotificationResponse> getAll();

    List<NotificationResponse> getByCorrelationId(UUID correlationId);
}