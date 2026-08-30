package com.logicore.orderservice.application.service;

import com.logicore.orderservice.application.dto.OrderResponse;
import com.logicore.orderservice.application.port.in.GetOrderUseCase;
import com.logicore.orderservice.application.port.out.OrderRepository;
import com.logicore.orderservice.domain.model.OrderId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service orchestrating order queries.
 */
@Service
public class GetOrderApplicationService implements GetOrderUseCase {

    private final OrderRepository orderRepository;

    public GetOrderApplicationService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrderResponse> getOrder(OrderId id) {
        return orderRepository.findById(id).map(OrderResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders() {
        return orderRepository.findAll().stream().map(OrderResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByCustomer(UUID customerId) {
        return orderRepository.findByCustomerId(customerId).stream().map(OrderResponse::from).toList();
    }
}