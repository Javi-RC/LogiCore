package com.logicore.common;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void createsMoneyWithTwoDecimalScale() {
        Money money = Money.of(new BigDecimal("10.5"));
        assertThat(money.amount()).isEqualByComparingTo("10.50");
        assertThat(money.currency()).isEqualTo("EUR");
    }

    @Test
    void addsMoneyOfSameCurrency() {
        Money a = Money.of(new BigDecimal("10.55"));
        Money b = Money.of(new BigDecimal("5.45"));
        assertThat(a.add(b).amount()).isEqualByComparingTo("16.00");
    }

    @Test
    void multipliesMoney() {
        Money unit = Money.of(new BigDecimal("3.99"));
        assertThat(unit.multiply(2).amount()).isEqualByComparingTo("7.98");
    }

    @Test
    void rejectsNullAmount() {
        assertThatThrownBy(() -> new Money(null, "EUR"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsBlankCurrency() {
        assertThatThrownBy(() -> Money.of(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsCurrencyMismatchOnAdd() {
        Money eur = Money.of(new BigDecimal("1.00"));
        Money usd = new Money(new BigDecimal("1.00"), "USD");
        assertThatThrownBy(() -> eur.add(usd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mismatch");
    }
}
