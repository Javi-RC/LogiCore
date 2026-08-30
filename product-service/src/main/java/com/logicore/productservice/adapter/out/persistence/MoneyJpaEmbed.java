package com.logicore.productservice.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

/**
 * Embeddable persistence component for a money price (amount + currency).
 * Demonstrates {@code @Embeddable} usage; maps to columns on {@code products}.
 */
@Embeddable
public class MoneyJpaEmbed {

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    public MoneyJpaEmbed() {
    }

    public MoneyJpaEmbed(BigDecimal amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }
}
