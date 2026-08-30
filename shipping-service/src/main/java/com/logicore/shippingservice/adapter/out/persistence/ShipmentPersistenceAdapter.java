package com.logicore.shippingservice.adapter.out.persistence;

import com.logicore.shippingservice.application.port.out.ShipmentRepository;
import com.logicore.shippingservice.domain.model.OrderId;
import com.logicore.shippingservice.domain.model.Shipment;
import com.logicore.shippingservice.domain.model.ShipmentId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Outbound adapter implementing {@link ShipmentRepository} backed by Spring Data JPA.
 */
@Component
public class ShipmentPersistenceAdapter implements ShipmentRepository {

    private final ShipmentJpaRepository jpaRepository;
    private final ShipmentPersistenceMapper mapper;

    public ShipmentPersistenceAdapter(ShipmentJpaRepository jpaRepository, ShipmentPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Shipment save(Shipment shipment) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(shipment)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Shipment> findById(ShipmentId shipmentId) {
        return jpaRepository.findById(shipmentId.value()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Shipment> findByOrderId(OrderId orderId) {
        return jpaRepository.findByOrderId(orderId.value()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Shipment> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }
}