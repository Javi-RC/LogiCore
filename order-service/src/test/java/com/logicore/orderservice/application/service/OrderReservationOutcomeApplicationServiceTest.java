package com.logicore.orderservice.application.service;

import com.logicore.common.DomainEvent;
import com.logicore.common.event.EventTypes;
import com.logicore.orderservice.domain.model.Money;
import com.logicore.orderservice.domain.model.Order;
import com.logicore.orderservice.domain.model.OrderId;
import com.logicore.orderservice.domain.model.OrderItem;
import com.logicore.orderservice.domain.model.OrderStatus;
import com.logicore.orderservice.domain.model.CustomerId;
import com.logicore.orderservice.application.port.out.OrderEventPublisher;
import com.logicore.orderservice.application.port.out.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderReservationOutcomeApplicationServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderEventPublisher eventPublisher;

    @InjectMocks
    private OrderReservationOutcomeApplicationService service;

    private Order pendingOrder() {
        OrderItem item = new OrderItem(UUID.randomUUID(), 1, Money.of(new BigDecimal("5.00")));
        return Order.create(OrderId.newId(), CustomerId.of(UUID.randomUUID()), List.of(item));
    }

    @Test
    void onStockReservedConfirmsOrder() {
        Order order = pendingOrder();
        when(orderRepository.findById(order.id())).thenReturn(Optional.of(order));
        when(orderRepository.save(org.mockito.ArgumentMatchers.any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = service.onStockReserved(order.id());

        assertThat(response.status()).isEqualTo(OrderStatus.CONFIRMED);
        ArgumentCaptor<DomainEvent<?>> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().eventType()).isEqualTo(EventTypes.ORDER_CONFIRMED);
    }

    @Test
    void onStockReservationFailedFailsOrder() {
        Order order = pendingOrder();
        when(orderRepository.findById(order.id())).thenReturn(Optional.of(order));
        when(orderRepository.save(org.mockito.ArgumentMatchers.any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = service.onStockReservationFailed(order.id());

        assertThat(response.status()).isEqualTo(OrderStatus.FAILED);
        ArgumentCaptor<DomainEvent<?>> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().eventType()).isEqualTo(EventTypes.ORDER_FAILED);
    }
}