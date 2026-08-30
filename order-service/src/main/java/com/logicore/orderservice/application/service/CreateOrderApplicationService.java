package com.logicore.orderservice.application.service;

import com.logicore.common.DomainEvent;
import com.logicore.common.event.EventTypes;
import com.logicore.common.event.OrderEventPayload;
import com.logicore.orderservice.application.command.CreateOrderCommand;
import com.logicore.orderservice.application.dto.OrderResponse;
import com.logicore.orderservice.application.port.in.CreateOrderUseCase;
import com.logicore.orderservice.application.port.out.OrderEventPublisher;
import com.logicore.orderservice.application.port.out.OrderRepository;
import com.logicore.orderservice.application.port.out.ProductClient;
import com.logicore.orderservice.domain.model.CustomerId;
import com.logicore.orderservice.domain.model.Money;
import com.logicore.orderservice.domain.model.Order;
import com.logicore.orderservice.domain.model.OrderId;
import com.logicore.orderservice.domain.model.OrderItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Application service orchestrating the {@code CreateOrder} use case.
 *
 * <p>The product price is copied from the catalog at creation time, satisfying the rule
 * that the order must not depend on later catalog price changes. After persisting the
 * PENDING order, an {@code OrderCreated} event is published to Kafka so that downstream
 * services (inventory) react asynchronously — this reaches eventual consistency
 * (saga-style choreography).</p>
 */
@Service
public class CreateOrderApplicationService implements CreateOrderUseCase {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final OrderEventPublisher eventPublisher;

    public CreateOrderApplicationService(OrderRepository orderRepository,
                                         ProductClient productClient,
                                         OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderCommand command) {
        List<OrderItem> items = new ArrayList<>(command.items().size());
        for (CreateOrderCommand.Item commandItem : command.items()) {
            ProductClient.ProductInfo product = productClient.getProduct(commandItem.productId());
            Money unitPrice = Money.of(product.price());
            items.add(new OrderItem(product.id(), commandItem.quantity(), unitPrice));
        }

        Order order = Order.create(OrderId.newId(), CustomerId.of(command.customerId()), items);
        Order saved = orderRepository.save(order);

        publishOrderCreated(saved);

        return OrderResponse.from(saved);
    }

    private void publishOrderCreated(Order order) {
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
        eventPublisher.publish(DomainEvent.of(EventTypes.ORDER_CREATED, order.id().value(), payload));
    }
}