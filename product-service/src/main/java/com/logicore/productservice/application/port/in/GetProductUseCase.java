package com.logicore.productservice.application.port.in;

import com.logicore.productservice.application.dto.ProductResponse;
import com.logicore.productservice.domain.model.ProductId;

import java.util.List;
import java.util.Optional;

/**
 * Inbound port: use case to query products.
 */
public interface GetProductUseCase {

    List<ProductResponse> getProducts();

    Optional<ProductResponse> getProduct(ProductId id);
}
