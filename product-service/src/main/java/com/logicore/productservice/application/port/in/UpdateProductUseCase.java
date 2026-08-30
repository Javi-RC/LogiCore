package com.logicore.productservice.application.port.in;

import com.logicore.productservice.application.command.UpdateProductCommand;
import com.logicore.productservice.application.dto.ProductResponse;
import com.logicore.productservice.domain.model.ProductId;

/**
 * Inbound port: use case to update or change the active state of a product.
 */
public interface UpdateProductUseCase {

    ProductResponse update(UpdateProductCommand command);

    ProductResponse deactivate(ProductId productId);

    ProductResponse activate(ProductId productId);
}
