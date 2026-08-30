package com.logicore.shippingservice.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data repository for {@link ShipmentJpaEntity}.
 */
@Repository
public interface ShipmentJpaRepository extends JpaRepository<ShipmentJpaEntity, UUID> {

    java.util.Optional<ShipmentJpaEntity> findByOrderId(UUID orderId);
}