package com.logicore.inventoryservice.adapter.out.persistence;

import com.logicore.inventoryservice.application.port.out.InventoryRepository;
import com.logicore.inventoryservice.domain.model.InventoryItem;
import com.logicore.inventoryservice.domain.model.ProductId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Outbound adapter implementing {@link InventoryRepository} backed by Spring Data JPA.
 *
 * <p>Updating an existing item requires the version to be carried from domain back to the
 * entity; Hibernate's {@code merge} performs a version check so concurrent writes fail with
 * an optimistic-locking exception instead of silently overwriting each other.</p>
 */
@Component
public class InventoryPersistenceAdapter implements InventoryRepository {

    private final InventoryItemJpaRepository jpaRepository;
    private final InventoryItemPersistenceMapper mapper;

    public InventoryPersistenceAdapter(InventoryItemJpaRepository jpaRepository,
                                       InventoryItemPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public InventoryItem save(InventoryItem item) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(item)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InventoryItem> findByProductId(ProductId productId) {
        return jpaRepository.findById(productId.value()).map(mapper::toDomain);
    }
}