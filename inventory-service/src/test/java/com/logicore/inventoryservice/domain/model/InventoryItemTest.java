package com.logicore.inventoryservice.domain.model;

import com.logicore.inventoryservice.domain.exception.InsufficientStockException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryItemTest {

    private static final ProductId PRODUCT = ProductId.of(UUID.randomUUID());

    @Test
    void createStartsWithZeroReserved() {
        InventoryItem item = InventoryItem.create(PRODUCT, 10);
        assertThat(item.availableQuantity()).isEqualTo(10);
        assertThat(item.reservedQuantity()).isZero();
        assertThat(item.version()).isNull();
    }

    @Test
    void rejectsNegativeQuantityOnCreate() {
        assertThatThrownBy(() -> InventoryItem.create(PRODUCT, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void reserveMovesQtyFromAvailableToReserved() {
        InventoryItem item = InventoryItem.create(PRODUCT, 10).reserve(4);
        assertThat(item.availableQuantity()).isEqualTo(6);
        assertThat(item.reservedQuantity()).isEqualTo(4);
    }

    @Test
    void reserveIsImmutable() {
        InventoryItem original = InventoryItem.create(PRODUCT, 10);
        InventoryItem reserved = original.reserve(3);
        assertThat(original.availableQuantity()).isEqualTo(10);
        assertThat(reserved.availableQuantity()).isEqualTo(7);
    }

    @Test
    void reserveBeyondAvailabilityThrows() {
        InventoryItem item = InventoryItem.create(PRODUCT, 2);
        assertThatThrownBy(() -> item.reserve(3))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("insufficient stock");
    }

    @Test
    void reserveRequiresPositiveQuantity() {
        assertThatThrownBy(() -> InventoryItem.create(PRODUCT, 10).reserve(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void releaseReturnsQtyToAvailable() {
        InventoryItem item = InventoryItem.create(PRODUCT, 5).reserve(5).release(2);
        assertThat(item.availableQuantity()).isEqualTo(2);
        assertThat(item.reservedQuantity()).isEqualTo(3);
    }

    @Test
    void releaseBeyondReservedThrows() {
        InventoryItem item = InventoryItem.create(PRODUCT, 5).reserve(2);
        assertThatThrownBy(() -> item.release(3))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("reserved");
    }

    @Test
    void confirmDropsReservedQty() {
        InventoryItem item = InventoryItem.create(PRODUCT, 5).reserve(5).confirm(5);
        assertThat(item.reservedQuantity()).isZero();
        assertThat(item.availableQuantity()).isZero();
    }

    @Test
    void confirmBeyondReservedThrows() {
        InventoryItem item = InventoryItem.create(PRODUCT, 5).reserve(2);
        assertThatThrownBy(() -> item.confirm(3))
                .isInstanceOf(IllegalStateException.class);
    }
}