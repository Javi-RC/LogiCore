package com.logicore.productservice.adapter.out.persistence;

import com.logicore.productservice.application.port.out.ProductRepository;
import com.logicore.productservice.domain.model.Product;
import com.logicore.productservice.domain.model.ProductId;
import com.logicore.productservice.domain.model.Sku;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Outbound adapter implementing {@link ProductRepository} backed by Spring Data JPA.
 */
@Component
public class ProductPersistenceAdapter implements ProductRepository {

    private final ProductJpaRepository jpaRepository;
    private final ProductPersistenceMapper mapper;

    public ProductPersistenceAdapter(ProductJpaRepository jpaRepository, ProductPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Product save(Product product) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(product)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findById(ProductId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findBySku(Sku sku) {
        return jpaRepository.findBySku(sku.value()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional
    public void deleteById(ProductId id) {
        jpaRepository.deleteById(id.value());
    }
}
