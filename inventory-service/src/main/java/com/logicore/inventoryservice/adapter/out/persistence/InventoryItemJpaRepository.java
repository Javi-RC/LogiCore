package com.logicore.inventoryservice.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data repository for {@link InventoryItemJpaEntity}.
 */
@Repository
public interface InventoryItemJpaRepository extends JpaRepository<InventoryItemJpaEntity, UUID> {
}