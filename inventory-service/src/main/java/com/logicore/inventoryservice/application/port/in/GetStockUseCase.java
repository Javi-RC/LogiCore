package com.logicore.inventoryservice.application.port.in;

import com.logicore.inventoryservice.domain.model.ProductId;

import java.util.Optional;

import com.logicore.inventoryservice.application.dto.InventoryItemResponse;

/**
 * Inbound port: query current stock levels for a product.
 */
public interface GetStockUseCase {

    Optional<InventoryItemResponse> getStock(ProductId productId);
}