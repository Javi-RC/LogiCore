package com.logicore.inventoryservice.application.service;

import com.logicore.common.DomainEvent;
import com.logicore.common.event.EventTypes;
import com.logicore.common.event.InventoryEventPayload;
import com.logicore.inventoryservice.application.command.ReleaseStockCommand;
import com.logicore.inventoryservice.application.port.in.ReleaseStockUseCase;
import com.logicore.inventoryservice.application.port.out.InventoryEventPublisher;
import com.logicore.inventoryservice.application.port.out.InventoryRepository;
import com.logicore.inventoryservice.domain.exception.InventoryItemNotFoundException;
import com.logicore.inventoryservice.domain.model.InventoryItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Releases previously reserved stock when an order is cancelled or fails, publishing
 * {@code StockReleased} as compensation.
 */
@Service
public class ReleaseStockApplicationService implements ReleaseStockUseCase {

    private final InventoryRepository inventoryRepository;
    private final InventoryEventPublisher eventPublisher;

    public ReleaseStockApplicationService(InventoryRepository inventoryRepository,
                                          InventoryEventPublisher eventPublisher) {
        this.inventoryRepository = inventoryRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public void release(ReleaseStockCommand command) {
        InventoryItem item = inventoryRepository.findByProductId(command.productId())
                .orElseThrow(() -> new InventoryItemNotFoundException(
                        "No inventory item registered for product " + command.productId()));

        InventoryItem released = item.release(command.quantity());
        inventoryRepository.save(released);

        eventPublisher.publish(DomainEvent.of(
                EventTypes.STOCK_RELEASED,
                UUID.fromString(command.correlationId()),
                new InventoryEventPayload(
                        UUID.fromString(command.correlationId()),
                        command.productId().value(),
                        command.quantity(),
                        true)));
    }
}