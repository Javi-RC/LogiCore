package com.logicore.shippingservice.application.service;

import com.logicore.shippingservice.application.command.MarkDeliveredCommand;
import com.logicore.shippingservice.application.dto.ShipmentResponse;
import com.logicore.shippingservice.application.port.in.MarkDeliveredUseCase;
import com.logicore.shippingservice.application.port.out.ShipmentRepository;
import com.logicore.shippingservice.domain.exception.ShipmentNotFoundException;
import com.logicore.shippingservice.domain.model.Shipment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Advances a shipment to delivered.
 */
@Service
public class MarkDeliveredApplicationService implements MarkDeliveredUseCase {

    private final ShipmentRepository shipmentRepository;

    public MarkDeliveredApplicationService(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @Override
    @Transactional
    public ShipmentResponse markDelivered(MarkDeliveredCommand command) {
        Shipment shipment = shipmentRepository.findById(command.shipmentId())
                .orElseThrow(() -> new ShipmentNotFoundException(
                        "Shipment " + command.shipmentId().value() + " not found"));

        return ShipmentResponse.from(shipmentRepository.save(shipment.deliver()));
    }
}