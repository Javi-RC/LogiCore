package com.logicore.notificationservice.adapter.out.persistence;

import com.logicore.notificationservice.application.port.out.NotificationRepository;
import com.logicore.notificationservice.domain.model.Notification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Outbound adapter implementing {@link NotificationRepository} backed by Spring Data JPA.
 */
@Component
public class NotificationPersistenceAdapter implements NotificationRepository {

    private final NotificationJpaRepository jpaRepository;
    private final NotificationPersistenceMapper mapper;

    public NotificationPersistenceAdapter(NotificationJpaRepository jpaRepository,
                                          NotificationPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Notification save(Notification notification) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(notification)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> findByCorrelationId(UUID correlationId) {
        return jpaRepository.findByCorrelationId(correlationId).stream().map(mapper::toDomain).toList();
    }
}