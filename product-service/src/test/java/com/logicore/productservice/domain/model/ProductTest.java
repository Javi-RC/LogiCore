package com.logicore.productservice.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    private static final Money PRICE = Money.of(new BigDecimal("10.00"));

    @Test
    void createsActiveProduct() {
        Product product = Product.create(ProductId.newId(), Sku.of("A1"), "Name", "Desc", PRICE);
        assertThat(product.active()).isTrue();
        assertThat(product.createdAt()).isNotNull();
    }

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> Product.create(ProductId.newId(), Sku.of("A1"), " ", null, PRICE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateProducesNewValues() {
        Product product = Product.create(ProductId.newId(), Sku.of("A1"), "Name", "Desc", PRICE);
        Product updated = product.update("New", "NewDesc", Money.of(new BigDecimal("20.00")));

        assertThat(updated.name()).isEqualTo("New");
        assertThat(updated.price().amount()).isEqualByComparingTo("20.00");
        assertThat(product.name()).isEqualTo("Name"); // original unchanged (immutability)
    }

    @Test
    void deactivateAndActivate() {
        Product product = Product.create(ProductId.newId(), Sku.of("A1"), "Name", null, PRICE);
        Product deactivated = product.deactivate();
        Product reactivated = deactivated.activate();

        assertThat(deactivated.active()).isFalse();
        assertThat(reactivated.active()).isTrue();
    }
}
