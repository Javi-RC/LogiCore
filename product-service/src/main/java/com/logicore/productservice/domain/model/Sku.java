package com.logicore.productservice.domain.model;

/**
 * Value object representing a stock keeping unit (SKU) — must be unique.
 */
public record Sku(String value) {

    public Sku {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("SKU must not be blank");
        }
        value = value.trim().toUpperCase();
        if (!value.matches("[A-Z0-9-]{1,50}")) {
            throw new IllegalArgumentException("SKU must contain only letters, digits and dashes (max 50)");
        }
    }

    public static Sku of(String value) {
        return new Sku(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
