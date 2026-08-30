package com.logicore.shippingservice.application.service;

import com.logicore.common.DomainEvent;
import com.logicore.common.event.EventTypes;
import com.logicore.common.event.ShipmentEventPayload;
import com.logicore.shippingservice.application.command.MarkShippedCommand;
import com.logicore.shippingservice.application.dto.ShipmentResponse;
import com.logicore.shippingservice.application.port.in.MarkShippedUseCase;
import com.logicore.shippingservice.application.port.out.ShipmentEventPublisher;
import com.logicore.shippingservice.application.port.out.ShipmentRepository;
import com.logicore.shippingservice.domain.exception.ShipmentNotFoundException;
import com.logicore.shippingservice.domain.model.Shipment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Advances a shipment to shipped and publishes {@code ShipmentShipped}.
 */
@Service
public class MarkShippedApplicationService implements MarkShippedUseCase {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentEventPublisher eventPublisher;

    public MarkShippedApplicationService(ShipmentRepository shipmentRepository,
                                         ShipmentEventPublisher eventPublisher) {
        this.shipmentRepository = shipmentRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public ShipmentResponse markShipped(MarkShippedCommand command) {
        Shipment shipment = shipmentRepository.findById(command.shipmentId())
                .orElseThrow(() -> new ShipmentNotFoundException(
                        "Shipment " + command.shipmentId().value() + " not found"));

        Shipment shipped = shipmentRepository.save(shipment.ship());

        eventPublisher.publish(DomainEvent.of(
                EventTypes.SHIPMENT_SHIPPED,
                shipped.orderId().value(),
                new ShipmentEventPayload(shipped.id().value(), shipped.orderId().value(), shipped.status().name())));

        return ShipmentResponse.from(shipped);
    }
}