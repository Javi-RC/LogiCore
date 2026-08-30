package com.logicore.notificationservice.adapter.in.web;

import com.logicore.notificationservice.application.dto.NotificationResponse;
import com.logicore.notificationservice.application.port.in.GetNotificationsUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST inbound adapter for querying notification history.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final GetNotificationsUseCase getNotificationsUseCase;

    public NotificationController(GetNotificationsUseCase getNotificationsUseCase) {
        this.getNotificationsUseCase = getNotificationsUseCase;
    }

    @GetMapping
    public List<NotificationResponse> getAll() {
        return getNotificationsUseCase.getAll();
    }

    @GetMapping("/correlation/{correlationId}")
    public List<NotificationResponse> getByCorrelationId(@PathVariable UUID correlationId) {
        return getNotificationsUseCase.getByCorrelationId(correlationId);
    }
}