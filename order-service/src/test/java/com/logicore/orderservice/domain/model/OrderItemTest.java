package com.logicore.orderservice.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderItemTest {

    @Test
    void computesSubtotal() {
        OrderItem item = new OrderItem(java.util.UUID.randomUUID(), 3, Money.of(new BigDecimal("2.50")));
        assertThat(item.subtotal().amount()).isEqualByComparingTo("7.50");
    }

    @Test
    void rejectsNonPositiveQuantity() {
        assertThatThrownBy(() -> new OrderItem(java.util.UUID.randomUUID(), 0, Money.of(new BigDecimal("1.00"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("quantity");
    }

    @Test
    void rejectsNullProductId() {
        assertThatThrownBy(() -> new OrderItem(null, 1, Money.of(new BigDecimal("1.00"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("productId");
    }

    @Test
    void rejectsNullUnitPrice() {
        assertThatThrownBy(() -> new OrderItem(java.util.UUID.randomUUID(), 1, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unitPrice");
    }
}