package com.logicore.inventoryservice.application.service;

import com.logicore.inventoryservice.application.dto.InventoryItemResponse;
import com.logicore.inventoryservice.application.port.in.GetStockUseCase;
import com.logicore.inventoryservice.application.port.out.InventoryRepository;
import com.logicore.inventoryservice.domain.model.ProductId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Queries current stock levels for a product.
 */
@Service
public class GetStockApplicationService implements GetStockUseCase {

    private final InventoryRepository inventoryRepository;

    public GetStockApplicationService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InventoryItemResponse> getStock(ProductId productId) {
        return inventoryRepository.findByProductId(productId).map(InventoryItemResponse::from);
    }
}