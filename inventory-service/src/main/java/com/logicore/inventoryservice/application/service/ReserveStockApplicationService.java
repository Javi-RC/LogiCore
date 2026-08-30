package com.logicore.inventoryservice.application.service;

import com.logicore.common.DomainEvent;
import com.logicore.common.event.EventTypes;
import com.logicore.common.event.InventoryEventPayload;
import com.logicore.inventoryservice.application.command.ReserveStockCommand;
import com.logicore.inventoryservice.application.dto.InventoryItemResponse;
import com.logicore.inventoryservice.application.port.in.ReserveStockUseCase;
import com.logicore.inventoryservice.application.port.out.InventoryEventPublisher;
import com.logicore.inventoryservice.application.port.out.InventoryRepository;
import com.logicore.inventoryservice.domain.exception.InsufficientStockException;
import com.logicore.inventoryservice.domain.exception.InventoryItemNotFoundException;
import com.logicore.inventoryservice.domain.model.InventoryItem;
import com.logicore.inventoryservice.domain.model.ProductId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Reserves stock for an order line and publishes {@code StockReserved} on success.
 *
 * <p>When available stock is insufficient, an {@link InsufficientStockException} is thrown
 * and the triggering adapter (Kafka consumer) is responsible for publishing the compensating
 * {@code StockReservationFailed} event.</p>
 *
 * <p>Relies on optimistic locking: concurrent reservations update the same {@code version}
 * and last-write-wins is prevented by Hibernate's version check.</p>
 */
@Service
public class ReserveStockApplicationService implements ReserveStockUseCase {

    private final InventoryRepository inventoryRepository;
    private final InventoryEventPublisher eventPublisher;

    public ReserveStockApplicationService(InventoryRepository inventoryRepository,
                                          InventoryEventPublisher eventPublisher) {
        this.inventoryRepository = inventoryRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public InventoryItemResponse reserve(ReserveStockCommand command) {
        InventoryItem item = inventoryRepository.findByProductId(command.productId())
                .orElseThrow(() -> new InventoryItemNotFoundException(
                        "No inventory item registered for product " + command.productId()));

        InventoryItem reserved = item.reserve(command.quantity());
        InventoryItem saved = inventoryRepository.save(reserved);

        eventPublisher.publish(DomainEvent.of(
                EventTypes.STOCK_RESERVED,
                UUID.fromString(command.correlationId()),
                new InventoryEventPayload(
                        UUID.fromString(command.correlationId()),
                        command.productId().value(),
                        command.quantity(),
                        true)));

        return InventoryItemResponse.from(saved);
    }
}