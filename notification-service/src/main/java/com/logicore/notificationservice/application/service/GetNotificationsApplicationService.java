package com.logicore.notificationservice.application.service;

import com.logicore.notificationservice.application.dto.NotificationResponse;
import com.logicore.notificationservice.application.port.in.GetNotificationsUseCase;
import com.logicore.notificationservice.application.port.out.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Queries notification history.
 */
@Service
public class GetNotificationsApplicationService implements GetNotificationsUseCase {

    private final NotificationRepository notificationRepository;

    public GetNotificationsApplicationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getAll() {
        return notificationRepository.findAll().stream().map(NotificationResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getByCorrelationId(UUID correlationId) {
        return notificationRepository.findByCorrelationId(correlationId).stream()
                .map(NotificationResponse::from)
                .toList();
    }
}