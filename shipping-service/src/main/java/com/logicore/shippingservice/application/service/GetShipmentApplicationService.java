package com.logicore.shippingservice.application.service;

import com.logicore.shippingservice.application.dto.ShipmentResponse;
import com.logicore.shippingservice.application.port.in.GetShipmentUseCase;
import com.logicore.shippingservice.application.port.out.ShipmentRepository;
import com.logicore.shippingservice.domain.model.OrderId;
import com.logicore.shippingservice.domain.model.ShipmentId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Queries shipments by id, order id, or returning all.
 */
@Service
public class GetShipmentApplicationService implements GetShipmentUseCase {

    private final ShipmentRepository shipmentRepository;

    public GetShipmentApplicationService(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ShipmentResponse> getById(ShipmentId shipmentId) {
        return shipmentRepository.findById(shipmentId).map(ShipmentResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ShipmentResponse> getByOrderId(OrderId orderId) {
        return shipmentRepository.findByOrderId(orderId).map(ShipmentResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShipmentResponse> getAll() {
        return shipmentRepository.findAll().stream().map(ShipmentResponse::from).toList();
    }
}