package com.logicore.orderservice.application.port.in;

import com.logicore.orderservice.application.dto.OrderResponse;
import com.logicore.orderservice.domain.model.OrderId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Inbound port: use case to query orders.
 */
public interface GetOrderUseCase {

    Optional<OrderResponse> getOrder(OrderId id);

    List<OrderResponse> getOrders();

    List<OrderResponse> getOrdersByCustomer(UUID customerId);
}