package com.logicore.productservice.adapter.out.persistence;

import com.logicore.productservice.domain.model.Money;
import com.logicore.productservice.domain.model.Product;
import com.logicore.productservice.domain.model.ProductId;
import com.logicore.productservice.domain.model.Sku;
import org.springframework.stereotype.Component;

/**
 * Maps between the domain {@link Product} and the persistence {@link ProductJpaEntity}.
 */
@Component
public class ProductPersistenceMapper {

    public ProductJpaEntity toEntity(Product product) {
        return new ProductJpaEntity(
                product.id().value(),
                product.sku().value(),
                product.name(),
                product.description(),
                new MoneyJpaEmbed(product.price().amount(), product.price().currency()),
                product.active(),
                product.createdAt()
        );
    }

    public Product toDomain(ProductJpaEntity entity) {
        return Product.rehydrate(
                ProductId.of(entity.getId()),
                Sku.of(entity.getSku()),
                entity.getName(),
                entity.getDescription(),
                new Money(entity.getPrice().getAmount(), entity.getPrice().getCurrency()),
                entity.isActive(),
                entity.getCreatedAt()
        );
    }
}
