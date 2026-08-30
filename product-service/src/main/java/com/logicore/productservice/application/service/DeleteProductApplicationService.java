package com.logicore.productservice.application.service;

import com.logicore.productservice.application.port.in.DeleteProductUseCase;
import com.logicore.productservice.application.port.out.ProductRepository;
import com.logicore.productservice.domain.model.ProductId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service orchestrating the {@code DeleteProduct} use case.
 */
@Service
public class DeleteProductApplicationService implements DeleteProductUseCase {

    private final ProductRepository productRepository;

    public DeleteProductApplicationService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void delete(ProductId id) {
        productRepository.deleteById(id);
    }
}
