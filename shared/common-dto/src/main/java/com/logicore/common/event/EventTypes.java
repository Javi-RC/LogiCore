package com.logicore.common.event;

/**
 * Central registry of event type strings and Kafka topics used across LogiCore services.
 *
 * <p>Keeping these centralized avoids typos and inconsistent names between producers and
 * consumers while introducing no framework or business-logic coupling.</p>
 */
public final class EventTypes {

    private EventTypes() {
    }

    // KAFKA TOPICS
    public static final String TOPIC_ORDER = "order-events";
    public static final String TOPIC_INVENTORY = "inventory-events";
    public static final String TOPIC_SHIPMENT = "shipment-events";
    public static final String TOPIC_NOTIFICATION = "notification-events";

    // ORDER EVENTS
    public static final String ORDER_CREATED = "OrderCreated";
    public static final String ORDER_CONFIRMED = "OrderConfirmed";
    public static final String ORDER_CANCELLED = "OrderCancelled";
    public static final String ORDER_FAILED = "OrderFailed";

    // INVENTORY EVENTS
    public static final String STOCK_RESERVED = "StockReserved";
    public static final String STOCK_RESERVATION_FAILED = "StockReservationFailed";
    public static final String STOCK_RELEASED = "StockReleased";

    // SHIPMENT EVENTS
    public static final String SHIPMENT_CREATED = "ShipmentCreated";
    public static final String SHIPMENT_SHIPPED = "ShipmentShipped";

    // NOTIFICATION TYPES
    public static final String NOTIFICATION_ORDER_CREATED = "ORDER_CREATED";
    public static final String NOTIFICATION_ORDER_CONFIRMED = "ORDER_CONFIRMED";
    public static final String NOTIFICATION_ORDER_CANCELLED = "ORDER_CANCELLED";
    public static final String NOTIFICATION_ORDER_FAILED = "ORDER_FAILED";
    public static final String NOTIFICATION_SHIPMENT_CREATED = "SHIPMENT_CREATED";
    public static final String NOTIFICATION_SHIPMENT_SHIPPED = "SHIPMENT_SHIPPED";
}
