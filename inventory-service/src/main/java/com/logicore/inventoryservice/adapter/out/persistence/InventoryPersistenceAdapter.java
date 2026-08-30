package com.logicore.inventoryservice.adapter.out.persistence;

import com.logicore.inventoryservice.application.port.out.InventoryRepository;
import com.logicore.inventoryservice.domain.exception.InventoryItemNotFoundException;
import com.logicore.inventoryservice.domain.model.InventoryItem;
import com.logicore.inventoryservice.domain.model.ProductId;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

/**
 * Outbound adapter implementing {@link InventoryRepository} backed by Spring Data JPA.
 *
 * <p>Existing items are updated by mutating the managed entity rather than rebuilding a
 * detached one, so the database-managed {@code createdAt} column is never overwritten with
 * {@code null}. The version carried by the domain item is checked against the persisted
 * version first; on a mismatch (concurrent write) an optimistic-locking exception is thrown
 * instead of silently overwriting the row.</p>
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
        if (item.version() == null) {
            return mapper.toDomain(jpaRepository.save(mapper.toEntity(item)));
        }
        InventoryItemJpaEntity entity = jpaRepository.findById(item.productId().value())
                .orElseThrow(() -> new InventoryItemNotFoundException(
                        "No inventory item registered for product " + item.productId()));
        if (!Objects.equals(entity.getVersion(), item.version())) {
            throw new ObjectOptimisticLockingFailureException(
                    InventoryItemJpaEntity.class, item.productId().value());
        }
        entity.setAvailableQuantity(item.availableQuantity());
        entity.setReservedQuantity(item.reservedQuantity());
        return mapper.toDomain(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InventoryItem> findByProductId(ProductId productId) {
        return jpaRepository.findById(productId.value()).map(mapper::toDomain);
    }
}