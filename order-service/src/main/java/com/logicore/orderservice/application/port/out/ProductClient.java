package com.logicore.orderservice.application.port.out;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Outbound port for fetching product information from the Product Service synchronously.
 * Used to validate product existence and copy the current price onto order items.
 */
public interface ProductClient {

    record ProductInfo(UUID id, String sku, String name, BigDecimal price) {}

    ProductInfo getProduct(UUID productId);
}
