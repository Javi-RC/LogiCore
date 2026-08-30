package com.logicore.inventoryservice.application.port.out;

import com.logicore.inventoryservice.domain.model.InventoryItem;
import com.logicore.inventoryservice.domain.model.ProductId;

import java.util.Optional;

/**
 * Outbound port for persisting and loading {@link InventoryItem} aggregates.
 */
public interface InventoryRepository {

    InventoryItem save(InventoryItem item);

    Optional<InventoryItem> findByProductId(ProductId productId);
}