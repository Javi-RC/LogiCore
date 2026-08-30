package com.logicore.orderservice.application.port.out;

import com.logicore.orderservice.domain.model.Order;
import com.logicore.orderservice.domain.model.OrderId;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for persisting and retrieving {@link Order}s.
 */
public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(OrderId id);

    List<Order> findByCustomerId(java.util.UUID customerId);

    List<Order> findAll();
}
