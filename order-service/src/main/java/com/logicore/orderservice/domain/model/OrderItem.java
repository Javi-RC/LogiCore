package com.logicore.orderservice.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Value object representing a single line item of an order.
 *
 * <p>The unit price is copied onto the item at creation time so the order does not depend
 * on later changes to the catalog price.</p>
 */
public record OrderItem(
        UUID productId,
        int quantity,
        Money unitPrice
) {

    public OrderItem {
        if (productId == null) {
            throw new IllegalArgumentException("productId must not be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (unitPrice == null) {
            throw new IllegalArgumentException("unitPrice must not be null");
        }
    }

    /**
     * Subtotal for this line: quantity * unitPrice.
     */
    public Money subtotal() {
        return unitPrice.multiply(quantity);
    }
}
