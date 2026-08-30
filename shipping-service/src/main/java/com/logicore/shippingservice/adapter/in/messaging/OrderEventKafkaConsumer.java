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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Inbound adapter: consumes order events and creates a shipment when an order is confirmed.
 *
 * <p>The Kafka value is deserialized to the raw {@link DomainEvent} envelope; the payload is
 * mapped with the injected {@link ObjectMapper}. Consumption is idempotent (see {@link ProcessedEventStore}).</p>
 */
@Component
public class OrderEventKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventKafkaConsumer.class);

    private final CreateShipmentUseCase createShipmentUseCase;
    private final ProcessedEventStore processedEventStore;
    private final ObjectMapper objectMapper;

    public OrderEventKafkaConsumer(CreateShipmentUseCase createShipmentUseCase,
                                   ProcessedEventStore processedEventStore,
                                   ObjectMapper objectMapper) {
        this.createShipmentUseCase = createShipmentUseCase;
        this.processedEventStore = processedEventStore;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = EventTypes.TOPIC_ORDER, groupId = "shipping-service")
    public void onOrderEvent(DomainEvent<?> event) {
        if (!EventTypes.ORDER_CONFIRMED.equals(event.eventType())) {
            return;
        }
        if (!processedEventStore.markIfAbsent(event.eventId())) {
            log.debug("Event {} already processed, skipping", event.eventId());
            return;
        }

        OrderEventPayload payload = objectMapper.convertValue(event.payload(), OrderEventPayload.class);
        createShipmentUseCase.createShipment(new CreateShipmentCommand(
                OrderId.of(payload.orderId()),
                CustomerId.of(payload.customerId())));
        log.info("Shipment created for confirmed order {}", payload.orderId());
    }
}