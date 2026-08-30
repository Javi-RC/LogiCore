package com.logicore.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Minimal shared money abstraction to avoid representing currency as a {@code double}.
 *
 * <p>This value type is shared across services for payloads that cross process boundaries
 * (e.g. prices in order events) but is intentionally <b>not</b> used as the internal domain
 * money type of each service's full business logic; services can define richer domain value
 * objects where appropriate. Shared here only because it is part of the JSON wire format.</p>
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
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount, DEFAULT_CURRENCY);
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    public Money multiply(int factor) {
        return new Money(amount.multiply(BigDecimal.valueOf(factor)), currency);
    }

    private void requireSameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("currency mismatch: " + currency + " vs " + other.currency);
        }
    }
}
