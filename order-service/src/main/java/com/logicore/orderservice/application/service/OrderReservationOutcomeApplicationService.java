package com.logicore.orderservice.application.service;

import com.logicore.common.DomainEvent;
import com.logicore.common.event.EventTypes;
import com.logicore.common.event.OrderEventPayload;
import com.logicore.orderservice.application.dto.OrderResponse;
import com.logicore.orderservice.application.port.in.OrderReservationOutcomeUseCase;
import com.logicore.orderservice.application.port.out.OrderEventPublisher;
import com.logicore.orderservice.application.port.out.OrderRepository;
import com.logicore.orderservice.domain.exception.OrderNotFoundException;
import com.logicore.orderservice.domain.model.Order;
import com.logicore.orderservice.domain.model.OrderId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service consuming the outcome of the inventory reservation (either
 * StockReserved or StockReservationFailed) and advancing the order state machine.
 */
@Service
public class OrderReservationOutcomeApplicationService implements OrderReservationOutcomeUseCase {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;

    public OrderReservationOutcomeApplicationService(OrderRepository orderRepository,
                                                     OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public OrderResponse onStockReserved(OrderId orderId) {
        Order order = findOrder(orderId);
        Order confirmed = order.confirm();
        Order saved = orderRepository.save(confirmed);
        publishOutcome(saved, EventTypes.ORDER_CONFIRMED);
        return OrderResponse.from(saved);
    }

    @Override
    @Transactional
    public OrderResponse onStockReservationFailed(OrderId orderId) {
        Order order = findOrder(orderId);
        Order failed = order.fail();
        Order saved = orderRepository.save(failed);
        publishOutcome(saved, EventTypes.ORDER_FAILED);
        return OrderResponse.from(saved);
    }

    private Order findOrder(OrderId orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order " + orderId.value() + " not found"));
    }

    private void publishOutcome(Order order, String eventType) {
        OrderEventPayload payload = new OrderEventPayload(
                order.id().value(),
                order.customerId().value(),
                order.status().name(),
                order.items().stream()
                        .map(item -> new OrderEventPayload.OrderItemPayload(
                                item.productId(),
                                item.quantity(),
                                item.unitPrice().amount().toPlainString()))
                        .toList()
        );
        eventPublisher.publish(DomainEvent.of(eventType, order.id().value(), payload));
    }
}