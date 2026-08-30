package com.logicore.shippingservice.adapter.out.persistence;

import com.logicore.shippingservice.domain.model.CustomerId;
import com.logicore.shippingservice.domain.model.OrderId;
import com.logicore.shippingservice.domain.model.Shipment;
import com.logicore.shippingservice.domain.model.ShipmentId;
import com.logicore.shippingservice.domain.model.ShipmentStatus;
import org.springframework.stereotype.Component;

/**
 * Maps between the {@link Shipment} domain aggregate and the JPA entity.
 */
@Component
public class ShipmentPersistenceMapper {

    public ShipmentJpaEntity toEntity(Shipment shipment) {
        return new ShipmentJpaEntity(
                shipment.id().value(),
                shipment.orderId().value(),
                shipment.customerId().value(),
                shipment.status().name());
    }

    public Shipment toDomain(ShipmentJpaEntity entity) {
        return Shipment.rehydrate(
                ShipmentId.of(entity.getId()),
                OrderId.of(entity.getOrderId()),
                CustomerId.of(entity.getCustomerId()),
                ShipmentStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}