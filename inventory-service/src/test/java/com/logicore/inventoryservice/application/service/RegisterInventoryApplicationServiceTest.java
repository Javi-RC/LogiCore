package com.logicore.inventoryservice.application.service;

import com.logicore.common.DomainEvent;
import com.logicore.common.event.EventTypes;
import com.logicore.inventoryservice.application.command.RegisterInventoryCommand;
import com.logicore.inventoryservice.application.dto.InventoryItemResponse;
import com.logicore.inventoryservice.application.port.out.InventoryRepository;
import com.logicore.inventoryservice.domain.model.InventoryItem;
import com.logicore.inventoryservice.domain.model.ProductId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterInventoryApplicationServiceTest {

    private static final ProductId PRODUCT = ProductId.of(UUID.randomUUID());

    @Mock
    private InventoryRepository inventoryRepository;

    private RegisterInventoryApplicationService service;

    @BeforeEach
    void setUp() {
        service = new RegisterInventoryApplicationService(inventoryRepository);
    }

    @Test
    void createsNewItemWhenNoneExists() {
        when(inventoryRepository.findByProductId(PRODUCT)).thenReturn(Optional.empty());
        when(inventoryRepository.save(any(InventoryItem.class))).thenAnswer(inv -> {
            InventoryItem item = inv.getArgument(0);
            return InventoryItem.rehydrate(item.productId(), item.availableQuantity(), item.reservedQuantity(), 1L);
        });

        InventoryItemResponse response = service.registerStock(new RegisterInventoryCommand(PRODUCT, 15));

        assertThat(response.availableQuantity()).isEqualTo(15);
        assertThat(response.reservedQuantity()).isZero();
    }

    @Test
    void addsToExistingItem() {
        InventoryItem existing = InventoryItem.rehydrate(PRODUCT, 10, 2, 4L);
        when(inventoryRepository.findByProductId(PRODUCT)).thenReturn(Optional.of(existing));
        when(inventoryRepository.save(any(InventoryItem.class))).thenAnswer(inv -> inv.getArgument(0));

        InventoryItemResponse response = service.registerStock(new RegisterInventoryCommand(PRODUCT, 5));

        assertThat(response.availableQuantity()).isEqualTo(15);
        assertThat(response.reservedQuantity()).isEqualTo(2);
    }
}