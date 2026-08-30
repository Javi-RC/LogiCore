package com.logicore.orderservice.application.service;

import com.logicore.common.DomainEvent;
import com.logicore.common.event.EventTypes;
import com.logicore.common.event.OrderEventPayload;
import com.logicore.orderservice.application.dto.OrderResponse;
import com.logicore.orderservice.application.port.in.CancelOrderUseCase;
import com.logicore.orderservice.application.port.out.OrderEventPublisher;
import com.logicore.orderservice.application.port.out.OrderRepository;
import com.logicore.orderservice.domain.exception.OrderNotFoundException;
import com.logicore.orderservice.domain.model.Order;
import com.logicore.orderservice.domain.model.OrderId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service orchestrating the {@code CancelOrder} use case.
 *
 * <p>Cancelling a confirmed order also needs a compensating stock release; that is handled
 * by the {@code OrderCancelled} event consumed by the Inventory Service.</p>
 */
@Service
public class CancelOrderApplicationService implements CancelOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;

    public CancelOrderApplicationService(OrderRepository orderRepository, OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public OrderResponse cancel(OrderId orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order " + orderId.value() + " not found"));

        Order cancelled = order.cancel();
        Order saved = orderRepository.save(cancelled);

        OrderEventPayload payload = new OrderEventPayload(
                saved.id().value(),
                saved.customerId().value(),
                saved.status().name(),
                saved.items().stream()
                        .map(item -> new OrderEventPayload.OrderItemPayload(
                                item.productId(),
                                item.quantity(),
                                item.unitPrice().amount().toPlainString()))
                        .toList()
        );
        eventPublisher.publish(DomainEvent.of(EventTypes.ORDER_CANCELLED, saved.id().value(), payload));

        return OrderResponse.from(saved);
    }
}