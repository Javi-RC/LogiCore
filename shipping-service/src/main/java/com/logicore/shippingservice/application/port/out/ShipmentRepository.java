package com.logicore.shippingservice.application.port.out;

import com.logicore.shippingservice.domain.model.OrderId;
import com.logicore.shippingservice.domain.model.Shipment;
import com.logicore.shippingservice.domain.model.ShipmentId;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for persisting and loading {@link Shipment} aggregates.
 */
public interface ShipmentRepository {

    Shipment save(Shipment shipment);

    Optional<Shipment> findById(ShipmentId shipmentId);

    Optional<Shipment> findByOrderId(OrderId orderId);

    List<Shipment> findAll();
}