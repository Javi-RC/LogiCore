package com.logicore.productservice.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Domain value object for money. Uses {@link BigDecimal} — never {@code double} — and
 * enforces the business invariant that a product price is non-negative.
 */
public record Money(BigDecimal amount, String currency) {

    public static final String DEFAULT_CURRENCY = "EUR";

    public Money {
        if (amount == null) {
            throw new IllegalArgumentException("amount must not be null");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("currency must not be blank");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount, DEFAULT_CURRENCY);
    }

    public BigDecimal amount() {
        return amount;
    }
}
