package com.logicore.shippingservice.application.service;

import com.logicore.common.DomainEvent;
import com.logicore.common.event.EventTypes;
import com.logicore.common.event.ShipmentEventPayload;
import com.logicore.shippingservice.application.command.CreateShipmentCommand;
import com.logicore.shippingservice.application.dto.ShipmentResponse;
import com.logicore.shippingservice.application.port.in.CreateShipmentUseCase;
import com.logicore.shippingservice.application.port.out.ShipmentEventPublisher;
import com.logicore.shippingservice.application.port.out.ShipmentRepository;
import com.logicore.shippingservice.domain.model.Shipment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates a shipment when an order is confirmed and publishes {@code ShipmentCreated}.
 */
@Service
public class CreateShipmentApplicationService implements CreateShipmentUseCase {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentEventPublisher eventPublisher;

    public CreateShipmentApplicationService(ShipmentRepository shipmentRepository,
                                            ShipmentEventPublisher eventPublisher) {
        this.shipmentRepository = shipmentRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public ShipmentResponse createShipment(CreateShipmentCommand command) {
        Shipment shipment = shipmentRepository.findByOrderId(command.orderId()).orElse(null);
        if (shipment != null) {
            return ShipmentResponse.from(shipment);
        }

        Shipment created = shipmentRepository.save(Shipment.create(command.orderId(), command.customerId()));

        eventPublisher.publish(DomainEvent.of(
                EventTypes.SHIPMENT_CREATED,
                command.orderId().value(),
                new ShipmentEventPayload(created.id().value(), command.orderId().value(), created.status().name())));

        return ShipmentResponse.from(created);
    }
}