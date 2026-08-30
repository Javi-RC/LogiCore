package com.logicore.shippingservice.adapter.out.messaging;

import com.logicore.common.DomainEvent;
import com.logicore.common.event.EventTypes;
import com.logicore.shippingservice.application.port.out.ShipmentEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Outbound adapter for {@link ShipmentEventPublisher} serializing events to the
 * {@code shipment-events} Kafka topic.
 */
@Component
public class KafkaShipmentEventPublisher implements ShipmentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaShipmentEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaShipmentEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(DomainEvent<?> event) {
        log.info("Publishing event {} (eventId={}, correlationId={}) to topic {}",
                event.eventType(), event.eventId(), event.correlationId(), EventTypes.TOPIC_SHIPMENT);
        kafkaTemplate.send(EventTypes.TOPIC_SHIPMENT, event.correlationId().toString(), event);
    }
}