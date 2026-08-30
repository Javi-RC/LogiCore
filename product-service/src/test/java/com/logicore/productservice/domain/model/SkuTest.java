package com.logicore.productservice.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkuTest {

    @Test
    void normalizesToUppercase() {
        Sku sku = Sku.of("abc-123");
        assertThat(sku.value()).isEqualTo("ABC-123");
    }

    @Test
    void rejectsBlank() {
        assertThatThrownBy(() -> Sku.of("  ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsInvalidCharacters() {
        assertThatThrownBy(() -> Sku.of("ab c!")).isInstanceOf(IllegalArgumentException.class);
    }
}
