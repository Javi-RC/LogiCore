package com.logicore.inventoryservice.application.service;

import com.logicore.inventoryservice.application.command.RegisterInventoryCommand;
import com.logicore.inventoryservice.application.dto.InventoryItemResponse;
import com.logicore.inventoryservice.application.port.in.RegisterStockUseCase;
import com.logicore.inventoryservice.application.port.out.InventoryRepository;
import com.logicore.inventoryservice.domain.model.InventoryItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Registers a new inventory item, or adjusts availability if the item already exists.
 */
@Service
public class RegisterInventoryApplicationService implements RegisterStockUseCase {

    private final InventoryRepository inventoryRepository;

    public RegisterInventoryApplicationService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    @Transactional
    public InventoryItemResponse registerStock(RegisterInventoryCommand command) {
        Optional<InventoryItem> existing = inventoryRepository.findByProductId(command.productId());
        InventoryItem item;
        if (existing.isPresent()) {
            item = existing.get();
            item = InventoryItem.rehydrate(
                    item.productId(),
                    item.availableQuantity() + command.quantity(),
                    item.reservedQuantity(),
                    item.version());
        } else {
            item = InventoryItem.create(command.productId(), command.quantity());
        }
        return InventoryItemResponse.from(inventoryRepository.save(item));
    }
}