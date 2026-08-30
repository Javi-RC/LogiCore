package com.logicore.orderservice.domain.model;

/**
 * Lifecycle states of an {@link Order}.
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    FAILED
}
