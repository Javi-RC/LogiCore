package com.logicore.inventoryservice.adapter.out.persistence;

import com.logicore.inventoryservice.domain.model.InventoryItem;
import com.logicore.inventoryservice.domain.model.ProductId;
import org.springframework.stereotype.Component;

/**
 * Maps between the {@link InventoryItem} domain aggregate and the JPA entity.
 */
@Component
public class InventoryItemPersistenceMapper {

    public InventoryItemJpaEntity toEntity(InventoryItem item) {
        return new InventoryItemJpaEntity(
                item.productId().value(),
                item.availableQuantity(),
                item.reservedQuantity(),
                item.version());
    }

    public InventoryItem toDomain(InventoryItemJpaEntity entity) {
        return InventoryItem.rehydrate(
                ProductId.of(entity.getProductId()),
                entity.getAvailableQuantity(),
                entity.getReservedQuantity(),
                entity.getVersion());
    }
}