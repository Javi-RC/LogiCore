package com.logicore.productservice.application.port.out;

import com.logicore.productservice.domain.model.Product;
import com.logicore.productservice.domain.model.ProductId;
import com.logicore.productservice.domain.model.Sku;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for persisting and retrieving {@link Product}s.
 */
public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(ProductId id);

    Optional<Product> findBySku(Sku sku);

    List<Product> findAll();

    void deleteById(ProductId id);
}
