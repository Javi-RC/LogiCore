package com.logicore.notificationservice.domain.model;

/**
 * Types of notifications the platform sends to customers. Mirrors the notification event
 * types in {@code com.logicore.common.event.EventTypes}.
 */
public enum NotificationType {
    ORDER_CREATED,
    ORDER_CONFIRMED,
    ORDER_CANCELLED,
    ORDER_FAILED,
    SHIPMENT_CREATED,
    SHIPMENT_SHIPPED
}