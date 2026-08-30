package com.logicore.notificationservice.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logicore.common.DomainEvent;
import com.logicore.common.event.EventTypes;
import com.logicore.common.event.OrderEventPayload;
import com.logicore.common.event.ShipmentEventPayload;
import com.logicore.notificationservice.application.command.RecordNotificationCommand;
import com.logicore.notificationservice.application.port.in.RecordNotificationUseCase;
import com.logicore.notificationservice.application.port.out.ProcessedEventStore;
import com.logicore.notificationservice.domain.model.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessEventKafkaConsumerTest {

    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID SHIPMENT_ID = UUID.randomUUID();

    @Mock
    private RecordNotificationUseCase recordNotificationUseCase;

    @Mock
    private ProcessedEventStore processedEventStore;

    private BusinessEventKafkaConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new BusinessEventKafkaConsumer(recordNotificationUseCase, processedEventStore, new ObjectMapper());
    }

    @Test
    void orderConfirmedRecordsNotification() {
        when(processedEventStore.markIfAbsent(any(UUID.class))).thenReturn(true);
        consumer.onOrderEvent(orderEvent(EventTypes.ORDER_CONFIRMED));

        ArgumentCaptor<RecordNotificationCommand> captor = ArgumentCaptor.forClass(RecordNotificationCommand.class);
        verify(recordNotificationUseCase).record(captor.capture());
        assertThat(captor.getValue().type()).isEqualTo(NotificationType.ORDER_CONFIRMED);
        assertThat(captor.getValue().correlationId()).isEqualTo(ORDER_ID);
        assertThat(captor.getValue().message()).contains(ORDER_ID.toString());
    }

    @Test
    void shipmentCreatedRecordsNotification() {
        when(processedEventStore.markIfAbsent(any(UUID.class))).thenReturn(true);
        consumer.onShipmentEvent(shipmentEvent(EventTypes.SHIPMENT_CREATED));

        ArgumentCaptor<RecordNotificationCommand> captor = ArgumentCaptor.forClass(RecordNotificationCommand.class);
        verify(recordNotificationUseCase).record(captor.capture());
        assertThat(captor.getValue().type()).isEqualTo(NotificationType.SHIPMENT_CREATED);
        assertThat(captor.getValue().correlationId()).isEqualTo(ORDER_ID);
    }

    @Test
    void unknownOrderEventIgnored() {
        consumer.onOrderEvent(orderEvent(EventTypes.ORDER_CREATED));
        consumer.onOrderEvent(new DomainEvent<Object>(
                UUID.randomUUID(), "UnknownEvent", Instant.now(), ORDER_ID, new Object()));

        verify(recordNotificationUseCase, never()).record(any());
    }

    @Test
    void alreadyProcessedEventSkipped() {
        when(processedEventStore.markIfAbsent(any(UUID.class))).thenReturn(false);
        consumer.onOrderEvent(orderEvent(EventTypes.ORDER_CONFIRMED));

        verify(recordNotificationUseCase, never()).record(any());
    }

    private DomainEvent<OrderEventPayload> orderEvent(String type) {
        return new DomainEvent<>(
                UUID.randomUUID(), type, Instant.now(), ORDER_ID,
                new OrderEventPayload(ORDER_ID, CUSTOMER_ID, "X", List.of()));
    }

    private DomainEvent<ShipmentEventPayload> shipmentEvent(String type) {
        return new DomainEvent<>(
                UUID.randomUUID(), type, Instant.now(), ORDER_ID,
                new ShipmentEventPayload(SHIPMENT_ID, ORDER_ID, "SHIPPED"));
    }
}