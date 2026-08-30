package com.logicore.notificationservice.adapter.out.persistence;

import com.logicore.notificationservice.domain.model.Notification;
import com.logicore.notificationservice.domain.model.NotificationId;
import com.logicore.notificationservice.domain.model.NotificationType;
import org.springframework.stereotype.Component;

/**
 * Maps between the {@link Notification} domain aggregate and the JPA entity.
 */
@Component
public class NotificationPersistenceMapper {

    public NotificationJpaEntity toEntity(Notification notification) {
        return new NotificationJpaEntity(
                notification.id().value(),
                notification.type().name(),
                notification.correlationId(),
                notification.recipient(),
                notification.message());
    }

    public Notification toDomain(NotificationJpaEntity entity) {
        return Notification.rehydrate(
                new NotificationId(entity.getId()),
                NotificationType.valueOf(entity.getType()),
                entity.getCorrelationId(),
                entity.getRecipient(),
                entity.getMessage(),
                entity.getCreatedAt());
    }
}