package com.logicore.productservice.application.service;

import com.logicore.productservice.application.command.UpdateProductCommand;
import com.logicore.productservice.application.dto.ProductResponse;
import com.logicore.productservice.application.port.in.UpdateProductUseCase;
import com.logicore.productservice.application.port.out.ProductRepository;
import com.logicore.productservice.domain.exception.ProductNotFoundException;
import com.logicore.productservice.domain.model.Money;
import com.logicore.productservice.domain.model.Product;
import com.logicore.productservice.domain.model.ProductId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service orchestrating product updates and (de)activation.
 */
@Service
public class UpdateProductApplicationService implements UpdateProductUseCase {

    private final ProductRepository productRepository;

    public UpdateProductApplicationService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public ProductResponse update(UpdateProductCommand command) {
        Product product = findOrThrow(command.id());
        Product updated = product.update(command.name(), command.description(), Money.of(command.price()));
        return ProductResponse.from(productRepository.save(updated));
    }

    @Override
    @Transactional
    public ProductResponse deactivate(ProductId productId) {
        Product product = findOrThrow(productId);
        return ProductResponse.from(productRepository.save(product.deactivate()));
    }

    @Override
    @Transactional
    public ProductResponse activate(ProductId productId) {
        Product product = findOrThrow(productId);
        return ProductResponse.from(productRepository.save(product.activate()));
    }

    private Product findOrThrow(ProductId id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product " + id.value() + " not found"));
    }
}
