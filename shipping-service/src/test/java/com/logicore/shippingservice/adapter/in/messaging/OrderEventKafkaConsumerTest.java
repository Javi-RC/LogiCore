package com.logicore.shippingservice.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logicore.common.DomainEvent;
import com.logicore.common.event.EventTypes;
import com.logicore.common.event.OrderEventPayload;
import com.logicore.shippingservice.application.command.CreateShipmentCommand;
import com.logicore.shippingservice.application.port.in.CreateShipmentUseCase;
import com.logicore.shippingservice.application.port.out.ProcessedEventStore;
import com.logicore.shippingservice.domain.model.CustomerId;
import com.logicore.shippingservice.domain.model.OrderId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderEventKafkaConsumerTest {

    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final UUID CUSTOMER_ID = UUID.randomUUID();

    @Mock
    private CreateShipmentUseCase createShipmentUseCase;

    @Mock
    private ProcessedEventStore processedEventStore;

    private OrderEventKafkaConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new OrderEventKafkaConsumer(createShipmentUseCase, processedEventStore, new ObjectMapper());
    }

    @Test
    void orderConfirmedCreatesShipment() {
        when(processedEventStore.markIfAbsent(any(UUID.class))).thenReturn(true);
        consumer.onOrderEvent(orderConfirmed());

        verify(createShipmentUseCase).createShipment(new CreateShipmentCommand(
                OrderId.of(ORDER_ID), CustomerId.of(CUSTOMER_ID)));
    }

    @Test
    void orderCreatedIsIgnored() {
        DomainEvent<?> created = new DomainEvent<>(
                UUID.randomUUID(), EventTypes.ORDER_CREATED, Instant.now(), ORDER_ID,
                new OrderEventPayload(ORDER_ID, CUSTOMER_ID, "PENDING", List.of()));

        consumer.onOrderEvent(created);

        verify(createShipmentUseCase, never()).createShipment(any());
    }

    @Test
    void alreadyProcessedEventIsSkipped() {
        when(processedEventStore.markIfAbsent(any(UUID.class))).thenReturn(false);
        consumer.onOrderEvent(orderConfirmed());

        verify(createShipmentUseCase, never()).createShipment(any());
    }

    private DomainEvent<?> orderConfirmed() {
        return new DomainEvent<>(
                UUID.randomUUID(), EventTypes.ORDER_CONFIRMED, Instant.now(), ORDER_ID,
                new OrderEventPayload(ORDER_ID, CUSTOMER_ID, "CONFIRMED", List.of()));
    }
}