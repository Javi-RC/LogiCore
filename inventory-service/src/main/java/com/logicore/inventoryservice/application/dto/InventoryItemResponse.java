package com.logicore.inventoryservice.application.dto;

import com.logicore.inventoryservice.domain.model.InventoryItem;
import com.logicore.inventoryservice.domain.model.ProductId;

/**
 * API view of an inventory item's current stock levels.
 */
public record InventoryItemResponse(ProductId productId, int availableQuantity, int reservedQuantity) {

    public static InventoryItemResponse from(InventoryItem item) {
        return new InventoryItemResponse(item.productId(), item.availableQuantity(), item.reservedQuantity());
    }
}