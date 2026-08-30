package com.logicore.inventoryservice.application.service;

import com.logicore.inventoryservice.application.command.ReserveStockCommand;
import com.logicore.inventoryservice.application.dto.InventoryItemResponse;
import com.logicore.inventoryservice.application.port.out.InventoryEventPublisher;
import com.logicore.inventoryservice.application.port.out.InventoryRepository;
import com.logicore.inventoryservice.domain.exception.InsufficientStockException;
import com.logicore.inventoryservice.domain.exception.InventoryItemNotFoundException;
import com.logicore.inventoryservice.domain.model.InventoryItem;
import com.logicore.inventoryservice.domain.model.ProductId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import com.logicore.common.DomainEvent;
import com.logicore.common.event.EventTypes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReserveStockApplicationServiceTest {

    private static final ProductId PRODUCT = ProductId.of(UUID.randomUUID());

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryEventPublisher eventPublisher;

    private ReserveStockApplicationService service;

    @BeforeEach
    void setUp() {
        service = new ReserveStockApplicationService(inventoryRepository, eventPublisher);
    }

    @Test
    void reserveUpdatesStockAndPublishesStockReserved() {
        InventoryItem item = InventoryItem.create(PRODUCT, 10);
        when(inventoryRepository.findByProductId(PRODUCT)).thenReturn(Optional.of(item));
        when(inventoryRepository.save(any(InventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

        InventoryItemResponse response = service.reserve(new ReserveStockCommand(
                UUID.randomUUID().toString(), PRODUCT, 4));

        assertThat(response.availableQuantity()).isEqualTo(6);
        assertThat(response.reservedQuantity()).isEqualTo(4);

        ArgumentCaptor<DomainEvent<?>> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().eventType()).isEqualTo(EventTypes.STOCK_RESERVED);
    }

    @Test
    void insufficientStockThrowsAndDoesNotPublish() {
        InventoryItem item = InventoryItem.create(PRODUCT, 1);
        when(inventoryRepository.findByProductId(PRODUCT)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> service.reserve(new ReserveStockCommand(
                UUID.randomUUID().toString(), PRODUCT, 5)))
                .isInstanceOf(InsufficientStockException.class);

        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void unknownProductThrows() {
        when(inventoryRepository.findByProductId(PRODUCT)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reserve(new ReserveStockCommand(
                UUID.randomUUID().toString(), PRODUCT, 1)))
                .isInstanceOf(InventoryItemNotFoundException.class);

        verify(eventPublisher, never()).publish(any());
    }
}