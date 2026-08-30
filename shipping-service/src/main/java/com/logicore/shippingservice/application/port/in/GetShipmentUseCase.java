package com.logicore.shippingservice.application.port.in;

import com.logicore.shippingservice.application.dto.ShipmentResponse;
import com.logicore.shippingservice.domain.model.OrderId;

import java.util.List;
import java.util.Optional;

import com.logicore.shippingservice.domain.model.ShipmentId;

/**
 * Inbound port: query shipments.
 */
public interface GetShipmentUseCase {

    Optional<ShipmentResponse> getById(ShipmentId shipmentId);

    Optional<ShipmentResponse> getByOrderId(OrderId orderId);

    List<ShipmentResponse> getAll();
}