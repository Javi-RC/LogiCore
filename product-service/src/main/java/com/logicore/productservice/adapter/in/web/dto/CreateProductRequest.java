package com.logicore.productservice.adapter.in.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Request DTO for creating a product.
 */
public record CreateProductRequest(
        @NotBlank(message = "sku is required")
        @Pattern(regexp = "[A-Za-z0-9-]{1,50}", message = "sku must contain only letters, digits and dashes (max 50)")
        String sku,

        @NotBlank(message = "name is required")
        @Size(max = 100, message = "name must be at most 100 characters")
        String name,

        @Size(max = 1000, message = "description must be at most 1000 characters")
        String description,

        @NotNull(message = "price is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "price must be non-negative")
        BigDecimal price
) {
}
