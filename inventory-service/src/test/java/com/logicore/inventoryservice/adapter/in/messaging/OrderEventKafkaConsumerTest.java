package com.logicore.inventoryservice.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logicore.common.DomainEvent;
import com.logicore.common.event.EventTypes;
import com.logicore.common.event.InventoryEventPayload;
import com.logicore.common.event.OrderEventPayload;
import com.logicore.inventoryservice.application.command.ReleaseStockCommand;
import com.logicore.inventoryservice.application.command.ReserveStockCommand;
import com.logicore.inventoryservice.application.port.in.ReleaseStockUseCase;
import com.logicore.inventoryservice.application.port.in.ReserveStockUseCase;
import com.logicore.inventoryservice.application.port.out.InventoryEventPublisher;
import com.logicore.inventoryservice.application.port.out.ProcessedEventStore;
import com.logicore.inventoryservice.domain.exception.InsufficientStockException;
import com.logicore.inventoryservice.domain.model.ProductId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderEventKafkaConsumerTest {

    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final UUID PRODUCT_A = UUID.randomUUID();
    private static final UUID PRODUCT_B = UUID.randomUUID();

    @Mock
    private ReserveStockUseCase reserveStockUseCase;

    @Mock
    private ReleaseStockUseCase releaseStockUseCase;

    @Mock
    private InventoryEventPublisher eventPublisher;

    @Mock
    private ProcessedEventStore processedEventStore;

    private OrderEventKafkaConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new OrderEventKafkaConsumer(reserveStockUseCase, releaseStockUseCase, eventPublisher,
                processedEventStore, new ObjectMapper());
    }

    @Test
    void reservesStockForEveryOrderLine() {
        when(processedEventStore.markIfAbsent(any(UUID.class))).thenReturn(true);
        DomainEvent<OrderEventPayload> event = orderCreated(ORDER_ID, PRODUCT_A, 2, PRODUCT_B, 1);

        consumer.onOrderEvent(event);

        verify(reserveStockUseCase).reserve(new ReserveStockCommand(
                ORDER_ID.toString(), ProductId.of(PRODUCT_A), 2));
        verify(reserveStockUseCase).reserve(new ReserveStockCommand(
                ORDER_ID.toString(), ProductId.of(PRODUCT_B), 1));
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void ignoresOtherOrderEventTypes() {
        DomainEvent<OrderEventPayload> confirmed = new DomainEvent<>(
                UUID.randomUUID(), EventTypes.ORDER_CONFIRMED, java.time.Instant.now(), ORDER_ID,
                new OrderEventPayload(ORDER_ID, UUID.randomUUID(), "CONFIRMED", List.of()));

        consumer.onOrderEvent(confirmed);

        verify(reserveStockUseCase, never()).reserve(any());
    }

    @Test
    void skipsAlreadyProcessedEvent() {
        when(processedEventStore.markIfAbsent(any(UUID.class))).thenReturn(false);

        consumer.onOrderEvent(orderCreated(ORDER_ID, PRODUCT_A, 2));

        verify(reserveStockUseCase, never()).reserve(any());
    }

    @Test
    void onInsufficientStockReleasesPreviousLinesAndPublishesFailure() {
        when(processedEventStore.markIfAbsent(any(UUID.class))).thenReturn(true);
        ProductId productA = ProductId.of(PRODUCT_A);
        ProductId productB = ProductId.of(PRODUCT_B);
        org.mockito.Mockito.lenient()
                .doThrow(new InsufficientStockException("no stock"))
                .when(reserveStockUseCase).reserve(new ReserveStockCommand(ORDER_ID.toString(), productB, 1));

        consumer.onOrderEvent(orderCreated(ORDER_ID, PRODUCT_A, 2, PRODUCT_B, 1));

        verify(reserveStockUseCase).reserve(new ReserveStockCommand(ORDER_ID.toString(), productA, 2));
        verify(releaseStockUseCase).release(new ReleaseStockCommand(ORDER_ID.toString(), productA, 2));

        ArgumentCaptor<DomainEvent<?>> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisher).publish(captor.capture());
        DomainEvent<?> published = captor.getValue();
        assertThat(published.eventType()).isEqualTo(EventTypes.STOCK_RESERVATION_FAILED);
        assertThat(published.correlationId()).isEqualTo(ORDER_ID);

        InventoryEventPayload payload = (InventoryEventPayload) published.payload();
        assertThat(payload.orderId()).isEqualTo(ORDER_ID);
        assertThat(payload.productId()).isEqualTo(PRODUCT_B);
        assertThat(payload.available()).isFalse();
    }

    private DomainEvent<OrderEventPayload> orderCreated(UUID orderId, Object... productQtyPairs) {
        UUID customerId = UUID.randomUUID();
        return new DomainEvent<>(
                UUID.randomUUID(), EventTypes.ORDER_CREATED, java.time.Instant.now(), orderId,
                new OrderEventPayload(orderId, customerId, "PENDING", orderLines(productQtyPairs)));
    }

    private List<OrderEventPayload.OrderItemPayload> orderLines(Object... pairs) {
        return java.util.stream.IntStream.range(0, pairs.length / 2)
                .mapToObj(i -> new OrderEventPayload.OrderItemPayload(
                        (UUID) pairs[i * 2], (Integer) pairs[i * 2 + 1], "10.00"))
                .toList();
    }
}