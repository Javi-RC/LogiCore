package com.logicore.inventoryservice.adapter.out.persistence;

import com.logicore.inventoryservice.application.port.out.ProcessedEventStore;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Outbound adapter implementing {@link ProcessedEventStore}. The primary-key uniqueness of
 * {@code processed_events.event_id} guarantees that only one concurrent consumer marks an
 * event as processed; duplicates are ignored.
 */
@Component
public class ProcessedEventPersistenceAdapter implements ProcessedEventStore {

    private final ProcessedEventJpaRepository jpaRepository;

    public ProcessedEventPersistenceAdapter(ProcessedEventJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public boolean markIfAbsent(UUID eventId) {
        if (jpaRepository.existsById(eventId)) {
            return false;
        }
        try {
            jpaRepository.save(new ProcessedEventJpaEntity(eventId, Instant.now()));
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }
}