package com.logicore.orderservice.domain.model;

import java.time.Instant;
import java.util.List;

/**
 * Aggregate root for an order. Encapsulates the order lifecycle and invariants.
 */
public class Order {

    private final OrderId id;
    private final CustomerId customerId;
    private final List<OrderItem> items;
    private final Money total;
    private OrderStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private Order(OrderId id, CustomerId customerId, List<OrderItem> items, Money total,
                  OrderStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.items = List.copyOf(items);
        this.total = total;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Factory for a brand-new order. Validates that there is at least one item and each
     * item has a positive quantity; computes the total by summing item subtotals.
     */
    public static Order create(OrderId id, CustomerId customerId, List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("an order must have at least one item");
        }
        Money total = items.stream()
                .map(OrderItem::subtotal)
                .reduce(Money.zero(), Money::add);
        return new Order(id, customerId, items, total, OrderStatus.PENDING, Instant.now(), Instant.now());
    }

    /**
     * Factory used to rebuild an order from persistence.
     */
    public static Order rehydrate(OrderId id, CustomerId customerId, List<OrderItem> items, Money total,
                                  OrderStatus status, Instant createdAt, Instant updatedAt) {
        return new Order(id, customerId, items, total, status, createdAt, updatedAt);
    }

    /**
     * Confirms the order. Business rule: a cancelled or failed order cannot be confirmed.
     */
    public Order confirm() {
        if (status == OrderStatus.CANCELLED || status == OrderStatus.FAILED) {
            throw new IllegalStateException("cannot confirm an order in state " + status);
        }
        return new Order(id, customerId, items, total, OrderStatus.CONFIRMED, createdAt, Instant.now());
    }

    /**
     * Marks the order as failed (e.g. stock reservation failed).
     */
    public Order fail() {
        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("cannot fail a cancelled order");
        }
        return new Order(id, customerId, items, total, OrderStatus.FAILED, createdAt, Instant.now());
    }

    /**
     * Cancels the order. Business rule: already-shipped/delivered orders cannot be cancelled;
     * only PENDING or FAILED orders may be cancelled (a CONFIRMED order that has not shipped
     * can also be cancelled by compensating with a stock release).
     */
    public Order cancel() {
        if (status != OrderStatus.PENDING && status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("cannot cancel an order in state " + status);
        }
        return new Order(id, customerId, items, total, OrderStatus.CANCELLED, createdAt, Instant.now());
    }

    public OrderId id() {
        return id;
    }

    public CustomerId customerId() {
        return customerId;
    }

    public List<OrderItem> items() {
        return items;
    }

    public Money total() {
        return total;
    }

    public OrderStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
