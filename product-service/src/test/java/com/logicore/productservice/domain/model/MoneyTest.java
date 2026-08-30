package com.logicore.productservice.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void createsMoneyWithTwoDecimalScale() {
        Money money = Money.of(new BigDecimal("9.999"));
        assertThat(money.amount()).isEqualByComparingTo("10.00");
    }

    @Test
    void rejectsNegativeAmount() {
        assertThatThrownBy(() -> Money.of(new BigDecimal("-1.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negative");
    }

    @Test
    void rejectsNullAmount() {
        assertThatThrownBy(() -> new Money(null, "EUR"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
