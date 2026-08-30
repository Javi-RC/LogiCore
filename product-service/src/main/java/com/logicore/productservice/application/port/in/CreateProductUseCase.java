package com.logicore.productservice.application.port.in;

import com.logicore.productservice.application.command.CreateProductCommand;
import com.logicore.productservice.application.dto.ProductResponse;

/**
 * Inbound port: use case to create a product.
 */
public interface CreateProductUseCase {

    ProductResponse create(CreateProductCommand command);
}
