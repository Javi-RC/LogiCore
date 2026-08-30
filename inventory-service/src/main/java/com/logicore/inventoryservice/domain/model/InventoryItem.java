package com.logicore.inventoryservice.domain.model;

import com.logicore.inventoryservice.domain.exception.InsufficientStockException;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain aggregate representing a product's stock. The {@code version} field is used to
 * implement optimistic locking at the persistence layer: concurrent modifications will be
 * detected by Hibernate's {@code @Version} and surfaced as conflicts.
 */
public class InventoryItem {

    private final ProductId productId;
    private int availableQuantity;
    private int reservedQuantity;
    private Long version;

    private InventoryItem(ProductId productId, int availableQuantity, int reservedQuantity, Long version) {
        this.productId = Objects.requireNonNull(productId, "productId must not be null");
        this.availableQuantity = availableQuantity;
        this.reservedQuantity = reservedQuantity;
        this.version = version;
    }

    public static InventoryItem create(ProductId productId, int availableQuantity) {
        if (availableQuantity < 0) {
            throw new IllegalArgumentException("availableQuantity must not be negative");
        }
        return new InventoryItem(productId, availableQuantity, 0, null);
    }

    public static InventoryItem rehydrate(ProductId productId, int availableQuantity, int reservedQuantity, Long version) {
        return new InventoryItem(productId, availableQuantity, reservedQuantity, version);
    }

    /**
     * Reserves {@code quantity} units of available stock.
     *
     * @throws InsufficientStockException if there is not enough available stock.
     */
    public InventoryItem reserve(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("reservation quantity must be positive");
        }
        if (availableQuantity - quantity < 0) {
            throw new InsufficientStockException(
                    "insufficient stock for product " + productId + ": requested " + quantity
                            + ", available " + availableQuantity);
        }
        return new InventoryItem(productId, availableQuantity - quantity, reservedQuantity + quantity, version);
    }

    /**
     * Releases {@code quantity} units previously reserved (compensation for cancelled/failed orders).
     */
    public InventoryItem release(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("release quantity must be positive");
        }
        if (reservedQuantity - quantity < 0) {
            throw new IllegalStateException("cannot release more than currently reserved");
        }
        return new InventoryItem(productId, availableQuantity + quantity, reservedQuantity - quantity, version);
    }

    /**
     * Confirms a reservation: moves {@code quantity} from reserved to permanently spent stock.
     * Simulated here by reducing both counters in a consistent way (available drops, reserved drops).
     */
    public InventoryItem confirm(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("confirm quantity must be positive");
        }
        if (reservedQuantity - quantity < 0) {
            throw new IllegalStateException("cannot confirm more than currently reserved");
        }
        return new InventoryItem(productId, availableQuantity, reservedQuantity - quantity, version);
    }

    public ProductId productId() {
        return productId;
    }

    public int availableQuantity() {
        return availableQuantity;
    }

    public int reservedQuantity() {
        return reservedQuantity;
    }

    public Long version() {
        return version;
    }
}