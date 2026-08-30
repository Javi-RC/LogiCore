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
import com.logicore.inventoryservice.domain.exception.InventoryItemNotFoundException;
import com.logicore.inventoryservice.domain.model.ProductId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Inbound adapter: consumes order events and reserves stock for {@code OrderCreated}.
 *
 * <p>Reservations are made per product line. If any line cannot be fulfilled, previously
 * reserved lines of the same order are released (compensation) and a
 * {@code StockReservationFailed} event is published so the Order Service can mark the order
 * as failed.</p>
 *
 * <p>The Kafka value is deserialized to the raw {@link DomainEvent} envelope; the payload is
 * mapped to its concrete type with the injected {@link ObjectMapper}. Consumption is
 * idempotent: each {@code eventId} is applied at most once (see {@link ProcessedEventStore}).</p>
 */
@Component
public class OrderEventKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventKafkaConsumer.class);

    private final ReserveStockUseCase reserveStockUseCase;
    private final ReleaseStockUseCase releaseStockUseCase;
    private final InventoryEventPublisher eventPublisher;
    private final ProcessedEventStore processedEventStore;
    private final ObjectMapper objectMapper;

    public OrderEventKafkaConsumer(ReserveStockUseCase reserveStockUseCase,
                                   ReleaseStockUseCase releaseStockUseCase,
                                   InventoryEventPublisher eventPublisher,
                                   ProcessedEventStore processedEventStore,
                                   ObjectMapper objectMapper) {
        this.reserveStockUseCase = reserveStockUseCase;
        this.releaseStockUseCase = releaseStockUseCase;
        this.eventPublisher = eventPublisher;
        this.processedEventStore = processedEventStore;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = EventTypes.TOPIC_ORDER, groupId = "inventory-service")
    public void onOrderEvent(DomainEvent<?> event) {
        if (!EventTypes.ORDER_CREATED.equals(event.eventType())) {
            return;
        }
        if (!processedEventStore.markIfAbsent(event.eventId())) {
            log.debug("Event {} already processed, skipping", event.eventId());
            return;
        }

        OrderEventPayload payload = objectMapper.convertValue(event.payload(), OrderEventPayload.class);
        UUID orderId = payload.orderId();
        Map<ProductId, Integer> reserved = new HashMap<>();

        for (OrderEventPayload.OrderItemPayload item : payload.items()) {
            ProductId productId = ProductId.of(item.productId());
            try {
                reserveStockUseCase.reserve(new ReserveStockCommand(
                        orderId.toString(), productId, item.quantity()));
                reserved.put(productId, item.quantity());
                log.info("Reserved {} units of {} for order {}", item.quantity(), productId, orderId);
            } catch (InsufficientStockException | InventoryItemNotFoundException ex) {
                log.warn("Stock reservation failed for order {} product {}: {}", orderId, productId, ex.getMessage());
                compensate(orderId, reserved);
                publishReservationFailed(orderId, productId.value(), item.quantity());
                return;
            }
        }
    }

    private void compensate(UUID orderId, Map<ProductId, Integer> reserved) {
        for (Map.Entry<ProductId, Integer> entry : reserved.entrySet()) {
            try {
                releaseStockUseCase.release(new ReleaseStockCommand(
                        orderId.toString(), entry.getKey(), entry.getValue()));
                log.info("Compensated reservation for order {}, product {}", orderId, entry.getKey());
            } catch (RuntimeException ex) {
                log.error("Compensation release failed for order {} product {}",
                        orderId, entry.getKey(), ex);
            }
        }
    }

    private void publishReservationFailed(UUID orderId, UUID productId, int quantity) {
        eventPublisher.publish(DomainEvent.of(
                EventTypes.STOCK_RESERVATION_FAILED,
                orderId,
                new InventoryEventPayload(orderId, productId, quantity, false)));
        log.info("Published {} for order {}", EventTypes.STOCK_RESERVATION_FAILED, orderId);
    }
}