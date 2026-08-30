package com.logicore.shippingservice.domain.model;

import com.logicore.shippingservice.domain.exception.InvalidShipmentStatusTransitionException;

import java.time.Instant;
import java.util.Objects;

/**
 * Shipment aggregate. Created when an order is confirmed, then transitions
 * {@code CREATED → SHIPPED → DELIVERED}. All state changes return new instances.
 */
public class Shipment {

    private final ShipmentId id;
    private final OrderId orderId;
    private final CustomerId customerId;
    private final ShipmentStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;

    private Shipment(ShipmentId id, OrderId orderId, CustomerId customerId, ShipmentStatus status,
                     Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.orderId = Objects.requireNonNull(orderId, "orderId must not be null");
        this.customerId = Objects.requireNonNull(customerId, "customerId must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Shipment create(OrderId orderId, CustomerId customerId) {
        Instant now = Instant.now();
        return new Shipment(ShipmentId.newId(), orderId, customerId, ShipmentStatus.CREATED, now, now);
    }

    public static Shipment rehydrate(ShipmentId id, OrderId orderId, CustomerId customerId,
                                     ShipmentStatus status, Instant createdAt, Instant updatedAt) {
        return new Shipment(id, orderId, customerId, status, createdAt, updatedAt);
    }

    public Shipment ship() {
        requireStatus(ShipmentStatus.CREATED, "cannot ship");
        return new Shipment(id, orderId, customerId, ShipmentStatus.SHIPPED, createdAt, Instant.now());
    }

    public Shipment deliver() {
        requireStatus(ShipmentStatus.SHIPPED, "cannot deliver");
        return new Shipment(id, orderId, customerId, ShipmentStatus.DELIVERED, createdAt, Instant.now());
    }

    private void requireStatus(ShipmentStatus expected, String action) {
        if (status != expected) {
            throw new InvalidShipmentStatusTransitionException(
                    action + " shipment " + id.value() + " from status " + status);
        }
    }

    public ShipmentId id() {
        return id;
    }

    public OrderId orderId() {
        return orderId;
    }

    public CustomerId customerId() {
        return customerId;
    }

    public ShipmentStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}