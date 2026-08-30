package com.logicore.productservice.application.service;

import com.logicore.productservice.application.dto.ProductResponse;
import com.logicore.productservice.application.port.in.GetProductUseCase;
import com.logicore.productservice.application.port.out.ProductRepository;
import com.logicore.productservice.domain.model.ProductId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Application service orchestrating product queries.
 */
@Service
public class GetProductApplicationService implements GetProductUseCase {

    private final ProductRepository productRepository;

    public GetProductApplicationService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getProducts() {
        return productRepository.findAll().stream().map(ProductResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductResponse> getProduct(ProductId id) {
        return productRepository.findById(id).map(ProductResponse::from);
    }
}
