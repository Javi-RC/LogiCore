package com.logicore.orderservice.adapter.out.messaging;

import com.logicore.common.DomainEvent;
import com.logicore.common.event.EventTypes;
import com.logicore.orderservice.application.port.out.OrderEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Outbound adapter implementing {@link OrderEventPublisher} by serializing events to the
 * {@code order-events} Kafka topic.
 *
 * <p>Events are published with the JSON value serialized by Spring for Kafka. Delivery is
 * at-least-once; consumers are responsible for idempotency (dedup by {@code eventId}).</p>
 */
@Component
public class KafkaOrderEventPublisher implements OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaOrderEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaOrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(DomainEvent<?> event) {
        log.info("Publishing event {} (eventId={}, correlationId={}) to topic {}",
                event.eventType(), event.eventId(), event.correlationId(), EventTypes.TOPIC_ORDER);
        kafkaTemplate.send(EventTypes.TOPIC_ORDER, event.correlationId().toString(), event);
    }
}