package com.logicore.orderservice.application.service;

import com.logicore.common.DomainEvent;
import com.logicore.common.event.EventTypes;
import com.logicore.orderservice.application.command.CreateOrderCommand;
import com.logicore.orderservice.application.dto.OrderResponse;
import com.logicore.orderservice.application.port.out.OrderEventPublisher;
import com.logicore.orderservice.application.port.out.OrderRepository;
import com.logicore.orderservice.application.port.out.ProductClient;
import com.logicore.orderservice.domain.model.Order;
import com.logicore.orderservice.domain.model.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateOrderApplicationServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductClient productClient;

    @Mock
    private OrderEventPublisher eventPublisher;

    @InjectMocks
    private CreateOrderApplicationService service;

    @Test
    void createsOrderCopiesPriceAndPublishesOrderCreated() {
        UUID productId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        when(productClient.getProduct(productId))
                .thenReturn(new ProductClient.ProductInfo(productId, "ABC-1", "Widget", new BigDecimal("10.00")));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = service.createOrder(new CreateOrderCommand(
                customerId,
                List.of(new CreateOrderCommand.Item(productId, 2))));

        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.total()).isEqualByComparingTo("20.00");
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).unitPrice()).isEqualByComparingTo("10.00");

        ArgumentCaptor<DomainEvent<?>> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue().eventType()).isEqualTo(EventTypes.ORDER_CREATED);
        assertThat(eventCaptor.getValue().correlationId()).isEqualTo(response.id());
    }
}