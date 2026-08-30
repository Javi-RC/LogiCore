package com.logicore.orderservice.adapter.out.persistence;

import com.logicore.orderservice.domain.model.Money;
import com.logicore.orderservice.domain.model.Order;
import com.logicore.orderservice.domain.model.OrderId;
import com.logicore.orderservice.domain.model.OrderItem;
import com.logicore.orderservice.domain.model.CustomerId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Maps between the domain {@link Order} aggregate and its persistence entities.
 */
@Component
public class OrderPersistenceMapper {

    public OrderJpaEntity toEntity(Order order) {
        OrderJpaEntity orderEntity = new OrderJpaEntity(
                order.id().value(),
                order.customerId().value(),
                null,
                new MoneyJpaEmbed(order.total().amount(), order.total().currency()),
                order.status(),
                order.createdAt(),
                order.updatedAt()
        );
        List<OrderItemJpaEntity> itemEntities = order.items().stream()
                .map(item -> new OrderItemJpaEntity(
                        UUID.randomUUID(),
                        orderEntity,
                        item.productId(),
                        item.quantity(),
                        new MoneyJpaEmbed(item.unitPrice().amount(), item.unitPrice().currency())))
                .toList();
        orderEntity.getItems().addAll(itemEntities);
        return orderEntity;
    }

    public Order toDomain(OrderJpaEntity entity) {
        List<OrderItem> items = entity.getItems() == null ? List.of() : entity.getItems().stream()
                .map(item -> new OrderItem(
                        item.getProductId(),
                        item.getQuantity(),
                        new Money(item.getUnitPrice().getAmount(), item.getUnitPrice().getCurrency())))
                .toList();
        return Order.rehydrate(
                OrderId.of(entity.getId()),
                CustomerId.of(entity.getCustomerId()),
                items,
                new Money(entity.getTotal().getAmount(), entity.getTotal().getCurrency()),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}