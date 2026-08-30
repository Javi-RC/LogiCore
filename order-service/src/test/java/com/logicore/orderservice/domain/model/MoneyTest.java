package com.logicore.orderservice.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void formatsToTwoDecimals() {
        assertThat(Money.of(new BigDecimal("10.555")).amount()).isEqualByComparingTo("10.56");
    }

    @Test
    void addsMatchingCurrencies() {
        Money sum = Money.of(new BigDecimal("1.10")).add(Money.of(new BigDecimal("2.90")));
        assertThat(sum.amount()).isEqualByComparingTo("4.00");
    }

    @Test
    void rejectsCurrencyMismatch() {
        assertThatThrownBy(() -> Money.of(new BigDecimal("1.00"))
                .add(new Money(new BigDecimal("1.00"), "USD")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("currency");
    }

    @Test
    void rejectsNegativeMultiplicationFactor() {
        assertThatThrownBy(() -> Money.of(new BigDecimal("1.00")).multiply(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}