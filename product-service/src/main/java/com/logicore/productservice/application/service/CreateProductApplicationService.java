package com.logicore.productservice.application.service;

import com.logicore.productservice.application.command.CreateProductCommand;
import com.logicore.productservice.application.dto.ProductResponse;
import com.logicore.productservice.application.port.in.CreateProductUseCase;
import com.logicore.productservice.application.port.out.ProductRepository;
import com.logicore.productservice.domain.exception.SkuAlreadyExistsException;
import com.logicore.productservice.domain.model.Money;
import com.logicore.productservice.domain.model.Product;
import com.logicore.productservice.domain.model.ProductId;
import com.logicore.productservice.domain.model.Sku;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service orchestrating the {@code CreateProduct} use case.
 */
@Service
public class CreateProductApplicationService implements CreateProductUseCase {

    private final ProductRepository productRepository;

    public CreateProductApplicationService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public ProductResponse create(CreateProductCommand command) {
        Sku sku = Sku.of(command.sku());
        productRepository.findBySku(sku).ifPresent(p -> {
            throw new SkuAlreadyExistsException("A product with SKU " + command.sku() + " already exists");
        });

        Product product = Product.create(
                ProductId.newId(),
                sku,
                command.name(),
                command.description(),
                Money.of(command.price())
        );
        return ProductResponse.from(productRepository.save(product));
    }
}
