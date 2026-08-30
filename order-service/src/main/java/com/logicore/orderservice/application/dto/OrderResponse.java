package com.logicore.orderservice.application.dto;

import com.logicore.orderservice.domain.model.Order;
import com.logicore.orderservice.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * API response DTO for an order.
 */
public record OrderResponse(
        UUID id,
        UUID customerId,
        List<OrderItemResponse> items,
        BigDecimal total,
        OrderStatus status,
        Instant createdAt,
        Instant updatedAt
) {

    public record OrderItemResponse(
            UUID productId,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {
    }

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.id().value(),
                order.customerId().value(),
                order.items().stream()
                        .map(item -> new OrderItemResponse(
                                item.productId(),
                                item.quantity(),
                                item.unitPrice().amount(),
                                item.subtotal().amount()))
                        .toList(),
                order.total().amount(),
                order.status(),
                order.createdAt(),
                order.updatedAt()
        );
    }
}