package com.logicore.productservice.domain.model;

import java.time.Instant;

/**
 * Domain aggregate representing a product in the catalog.
 */
public class Product {

    private final ProductId id;
    private final Sku sku;
    private String name;
    private String description;
    private Money price;
    private boolean active;
    private final Instant createdAt;

    private Product(ProductId id, Sku sku, String name, String description, Money price, boolean active, Instant createdAt) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.price = price;
        this.active = active;
        this.createdAt = createdAt;
    }

    public static Product create(ProductId id, Sku sku, String name, String description, Money price) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (price == null) {
            throw new IllegalArgumentException("price must not be null");
        }
        return new Product(id, sku, name, description, price, true, Instant.now());
    }

    public static Product rehydrate(ProductId id, Sku sku, String name, String description, Money price, boolean active, Instant createdAt) {
        return new Product(id, sku, name, description, price, active, createdAt);
    }

    public Product update(String name, String description, Money price) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (price == null) {
            throw new IllegalArgumentException("price must not be null");
        }
        return new Product(id, sku, name, description, price, active, createdAt);
    }

    public Product deactivate() {
        return new Product(id, sku, name, description, price, false, createdAt);
    }

    public Product activate() {
        return new Product(id, sku, name, description, price, true, createdAt);
    }

    public ProductId id() {
        return id;
    }

    public Sku sku() {
        return sku;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public Money price() {
        return price;
    }

    public boolean active() {
        return active;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
