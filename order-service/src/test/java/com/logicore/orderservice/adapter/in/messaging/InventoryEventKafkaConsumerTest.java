package com.logicore.orderservice.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logicore.common.DomainEvent;
import com.logicore.common.event.EventTypes;
import com.logicore.common.event.InventoryEventPayload;
import com.logicore.orderservice.application.port.in.OrderReservationOutcomeUseCase;
import com.logicore.orderservice.application.port.out.ProcessedEventStore;
import com.logicore.orderservice.domain.model.OrderId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryEventKafkaConsumerTest {

    private static final UUID ORDER_ID = UUID.randomUUID();

    @Mock
    private OrderReservationOutcomeUseCase orderReservationOutcomeUseCase;

    @Mock
    private ProcessedEventStore processedEventStore;

    private InventoryEventKafkaConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new InventoryEventKafkaConsumer(orderReservationOutcomeUseCase, processedEventStore, new ObjectMapper());
    }

    @Test
    void stockReservedConfirmsOrder() {
        when(processedEventStore.markIfAbsent(any(UUID.class))).thenReturn(true);
        consumer.onInventoryEvent(inventoryEvent(EventTypes.STOCK_RESERVED, true));

        verify(orderReservationOutcomeUseCase).onStockReserved(OrderId.of(ORDER_ID));
    }

    @Test
    void stockReservationFailedFailsOrder() {
        when(processedEventStore.markIfAbsent(any(UUID.class))).thenReturn(true);
        consumer.onInventoryEvent(inventoryEvent(EventTypes.STOCK_RESERVATION_FAILED, false));

        verify(orderReservationOutcomeUseCase).onStockReservationFailed(OrderId.of(ORDER_ID));
    }

    @Test
    void alreadyProcessedEventIsSkipped() {
        when(processedEventStore.markIfAbsent(any(UUID.class))).thenReturn(false);
        consumer.onInventoryEvent(inventoryEvent(EventTypes.STOCK_RESERVED, true));

        verify(orderReservationOutcomeUseCase, never()).onStockReserved(any());
    }

    @Test
    void unknownEventTypeIsIgnored() {
        when(processedEventStore.markIfAbsent(any(UUID.class))).thenReturn(true);
        consumer.onInventoryEvent(inventoryEvent(EventTypes.STOCK_RELEASED, true));

        verify(orderReservationOutcomeUseCase, never()).onStockReserved(any());
        verify(orderReservationOutcomeUseCase, never()).onStockReservationFailed(any());
    }

    private DomainEvent<InventoryEventPayload> inventoryEvent(String type, boolean available) {
        return new DomainEvent<>(
                UUID.randomUUID(), type, Instant.now(), ORDER_ID,
                new InventoryEventPayload(ORDER_ID, UUID.randomUUID(), 1, available));
    }
}