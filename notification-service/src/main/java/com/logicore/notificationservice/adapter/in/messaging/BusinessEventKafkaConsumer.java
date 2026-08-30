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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Inbound adapter: consumes order and shipment events and records a notification for them.
 *
 * <p>The Kafka value is deserialized to the raw {@link DomainEvent} envelope; the payload is
 * mapped with the injected {@link ObjectMapper}. Consumption is idempotent (see {@link ProcessedEventStore}).</p>
 */
@Component
public class BusinessEventKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(BusinessEventKafkaConsumer.class);

    private static final Map<String, NotificationType> ORDER_EVENT_TO_NOTIFICATION = Map.of(
            EventTypes.ORDER_CREATED, NotificationType.ORDER_CREATED,
            EventTypes.ORDER_CONFIRMED, NotificationType.ORDER_CONFIRMED,
            EventTypes.ORDER_CANCELLED, NotificationType.ORDER_CANCELLED,
            EventTypes.ORDER_FAILED, NotificationType.ORDER_FAILED);

    private static final Map<String, NotificationType> SHIPMENT_EVENT_TO_NOTIFICATION = Map.of(
            EventTypes.SHIPMENT_CREATED, NotificationType.SHIPMENT_CREATED,
            EventTypes.SHIPMENT_SHIPPED, NotificationType.SHIPMENT_SHIPPED);

    private final RecordNotificationUseCase recordNotificationUseCase;
    private final ProcessedEventStore processedEventStore;
    private final ObjectMapper objectMapper;

    public BusinessEventKafkaConsumer(RecordNotificationUseCase recordNotificationUseCase,
                                      ProcessedEventStore processedEventStore,
                                      ObjectMapper objectMapper) {
        this.recordNotificationUseCase = recordNotificationUseCase;
        this.processedEventStore = processedEventStore;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = EventTypes.TOPIC_ORDER, groupId = "notification-service")
    public void onOrderEvent(DomainEvent<?> event) {
        NotificationType type = ORDER_EVENT_TO_NOTIFICATION.get(event.eventType());
        if (type == null) {
            return;
        }
        if (!processedEventStore.markIfAbsent(event.eventId())) {
            log.debug("Event {} already processed, skipping", event.eventId());
            return;
        }

        OrderEventPayload payload = objectMapper.convertValue(event.payload(), OrderEventPayload.class);
        recordNotificationUseCase.record(new RecordNotificationCommand(
                type,
                payload.orderId(),
                "customer-" + payload.customerId(),
                orderMessage(type, payload.orderId())));
    }

    @KafkaListener(topics = EventTypes.TOPIC_SHIPMENT, groupId = "notification-service")
    public void onShipmentEvent(DomainEvent<?> event) {
        NotificationType type = SHIPMENT_EVENT_TO_NOTIFICATION.get(event.eventType());
        if (type == null) {
            return;
        }
        if (!processedEventStore.markIfAbsent(event.eventId())) {
            log.debug("Event {} already processed, skipping", event.eventId());
            return;
        }

        ShipmentEventPayload payload = objectMapper.convertValue(event.payload(), ShipmentEventPayload.class);
        recordNotificationUseCase.record(new RecordNotificationCommand(
                type,
                payload.orderId(),
                "customer-of-order-" + payload.orderId(),
                shipmentMessage(type, payload.shipmentId(), payload.orderId())));
    }

    private String orderMessage(NotificationType type, UUID orderId) {
        return switch (type) {
            case ORDER_CREATED -> "Your order " + orderId + " has been received.";
            case ORDER_CONFIRMED -> "Your order " + orderId + " has been confirmed.";
            case ORDER_CANCELLED -> "Your order " + orderId + " has been cancelled.";
            case ORDER_FAILED -> "Your order " + orderId + " could not be fulfilled.";
            default -> "Update for order " + orderId;
        };
    }

    private String shipmentMessage(NotificationType type, UUID shipmentId, UUID orderId) {
        return switch (type) {
            case SHIPMENT_CREATED -> "A shipment " + shipmentId + " was created for order " + orderId + ".";
            case SHIPMENT_SHIPPED -> "Shipment " + shipmentId + " (order " + orderId + ") is on its way.";
            default -> "Update for shipment " + shipmentId;
        };
    }
}