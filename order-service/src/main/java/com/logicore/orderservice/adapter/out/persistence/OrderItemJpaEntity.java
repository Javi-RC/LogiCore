package com.logicore.orderservice.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Persistence entity for an order line item. Owned by {@link OrderJpaEntity}.
 */
@Entity
@Table(name = "order_items")
public class OrderItemJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderJpaEntity order;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private int quantity;

    @Embedded
    private MoneyJpaEmbed unitPrice;

    protected OrderItemJpaEntity() {
    }

    public OrderItemJpaEntity(UUID id, OrderJpaEntity order, UUID productId, int quantity, MoneyJpaEmbed unitPrice) {
        this.id = id;
        this.order = order;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public UUID getId() {
        return id;
    }

    public OrderJpaEntity getOrder() {
        return order;
    }

    public UUID getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public MoneyJpaEmbed getUnitPrice() {
        return unitPrice;
    }
}