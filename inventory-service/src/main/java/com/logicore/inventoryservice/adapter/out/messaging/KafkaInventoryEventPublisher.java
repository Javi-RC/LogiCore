package com.logicore.inventoryservice.adapter.out.messaging;

import com.logicore.common.DomainEvent;
import com.logicore.common.event.EventTypes;
import com.logicore.inventoryservice.application.port.out.InventoryEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Outbound adapter for {@link InventoryEventPublisher} serializing events to the
 * {@code inventory-events} Kafka topic.
 */
@Component
public class KafkaInventoryEventPublisher implements InventoryEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaInventoryEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaInventoryEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(DomainEvent<?> event) {
        log.info("Publishing event {} (eventId={}, correlationId={}) to topic {}",
                event.eventType(), event.eventId(), event.correlationId(), EventTypes.TOPIC_INVENTORY);
        kafkaTemplate.send(EventTypes.TOPIC_INVENTORY, event.correlationId().toString(), event);
    }
}