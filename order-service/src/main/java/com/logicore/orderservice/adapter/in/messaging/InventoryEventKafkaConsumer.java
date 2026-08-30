package com.logicore.orderservice.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logicore.common.DomainEvent;
import com.logicore.common.event.EventTypes;
import com.logicore.common.event.InventoryEventPayload;
import com.logicore.orderservice.application.port.in.OrderReservationOutcomeUseCase;
import com.logicore.orderservice.application.port.out.ProcessedEventStore;
import com.logicore.orderservice.domain.model.OrderId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Inbound adapter: consumes inventory events and advances the order state machine.
 * {@code StockReserved} confirms the order; {@code StockReservationFailed} fails it.
 *
 * <p>The Kafka value is deserialized to the raw {@link DomainEvent} envelope (its generic
 * payload arrives as a JSON object); the payload is then mapped to its concrete type with
 * the injected {@link ObjectMapper}. Consumption is idempotent: each {@code eventId} is
 * applied at most once (see {@link ProcessedEventStore}).</p>
 */
@Component
public class InventoryEventKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventKafkaConsumer.class);

    private final OrderReservationOutcomeUseCase orderReservationOutcomeUseCase;
    private final ProcessedEventStore processedEventStore;
    private final ObjectMapper objectMapper;

    public InventoryEventKafkaConsumer(OrderReservationOutcomeUseCase orderReservationOutcomeUseCase,
                                       ProcessedEventStore processedEventStore,
                                       ObjectMapper objectMapper) {
        this.orderReservationOutcomeUseCase = orderReservationOutcomeUseCase;
        this.processedEventStore = processedEventStore;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = EventTypes.TOPIC_INVENTORY, groupId = "order-service")
    public void onInventoryEvent(DomainEvent<?> event) {
        if (!processedEventStore.markIfAbsent(event.eventId())) {
            log.debug("Event {} already processed, skipping", event.eventId());
            return;
        }

        InventoryEventPayload payload = objectMapper.convertValue(event.payload(), InventoryEventPayload.class);
        OrderId orderId = OrderId.of(payload.orderId());
        switch (event.eventType()) {
            case EventTypes.STOCK_RESERVED -> {
                orderReservationOutcomeUseCase.onStockReserved(orderId);
                log.info("Order {} confirmed after stock reservation", orderId.value());
            }
            case EventTypes.STOCK_RESERVATION_FAILED -> {
                orderReservationOutcomeUseCase.onStockReservationFailed(orderId);
                log.info("Order {} failed because stock could not be reserved", orderId.value());
            }
            default -> log.debug("Ignoring inventory event type {}", event.eventType());
        }
    }
}