package com.logicore.notificationservice.application.service;

import com.logicore.notificationservice.application.command.RecordNotificationCommand;
import com.logicore.notificationservice.application.port.in.RecordNotificationUseCase;
import com.logicore.notificationservice.application.port.out.NotificationRepository;
import com.logicore.notificationservice.domain.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Records a notification produced from an order/shipment event and dispatches it
 * (in this reference implementation, dispatch is simulated by persisting + logging).
 */
@Service
public class RecordNotificationApplicationService implements RecordNotificationUseCase {

    private static final Logger log = LoggerFactory.getLogger(RecordNotificationApplicationService.class);

    private final NotificationRepository notificationRepository;

    public RecordNotificationApplicationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public void record(RecordNotificationCommand command) {
        Notification notification = Notification.create(
                command.type(), command.correlationId(), command.recipient(), command.message());
        notificationRepository.save(notification);
        log.info("Notification {} sent to {} for correlation {}: {}",
                command.type(), command.recipient(), command.correlationId(), command.message());
    }
}