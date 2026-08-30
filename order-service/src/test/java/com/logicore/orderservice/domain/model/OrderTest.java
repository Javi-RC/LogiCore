package com.logicore.orderservice.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private static final UUID PRODUCT_A = UUID.randomUUID();
    private static final UUID PRODUCT_B = UUID.randomUUID();

    private static List<OrderItem> twoItems() {
        return List.of(
                new OrderItem(PRODUCT_A, 2, Money.of(new BigDecimal("10.00"))),
                new OrderItem(PRODUCT_B, 1, Money.of(new BigDecimal("5.50"))));
    }

    @Test
    void createsOrderInPendingStateWithCorrectTotal() {
        Order order = Order.create(OrderId.newId(), CustomerId.of(UUID.randomUUID()), twoItems());

        assertThat(order.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.total().amount()).isEqualByComparingTo("25.50");
        assertThat(order.items()).hasSize(2);
    }

    @Test
    void rejectsEmptyItems() {
        assertThatThrownBy(() -> Order.create(OrderId.newId(), CustomerId.of(UUID.randomUUID()), List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one item");
    }

    @Test
    void confirmsPendingOrder() {
        Order order = Order.create(OrderId.newId(), CustomerId.of(UUID.randomUUID()), twoItems());
        Order confirmed = order.confirm();
        assertThat(confirmed.status()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(order.status()).isEqualTo(OrderStatus.PENDING); // immutable transform
    }

    @Test
    void cannotConfirmCancelledOrder() {
        Order order = Order.create(OrderId.newId(), CustomerId.of(UUID.randomUUID()), twoItems());
        Order cancelled = order.cancel();
        assertThatThrownBy(cancelled::confirm)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot confirm");
    }

    @Test
    void cannotConfirmFailedOrder() {
        Order order = Order.create(OrderId.newId(), CustomerId.of(UUID.randomUUID()), twoItems());
        Order failed = order.fail();
        assertThatThrownBy(failed::confirm)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cancelsPendingOrder() {
        Order order = Order.create(OrderId.newId(), CustomerId.of(UUID.randomUUID()), twoItems());
        Order cancelled = order.cancel();
        assertThat(cancelled.status()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void cannotCancelFailedOrder() {
        Order order = Order.create(OrderId.newId(), CustomerId.of(UUID.randomUUID()), twoItems());
        Order failed = order.fail();
        assertThatThrownBy(failed::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot cancel");
    }

    @Test
    void failsPendingOrder() {
        Order order = Order.create(OrderId.newId(), CustomerId.of(UUID.randomUUID()), twoItems());
        Order failed = order.fail();
        assertThat(failed.status()).isEqualTo(OrderStatus.FAILED);
    }
}