package com.logicore.productservice.application.port.in;

import com.logicore.productservice.domain.model.ProductId;

/**
 * Inbound port: use case to delete a product.
 */
public interface DeleteProductUseCase {

    void delete(ProductId id);
}
