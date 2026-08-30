package com.logicore.shippingservice.application.service;

import com.logicore.common.DomainEvent;
import com.logicore.common.event.EventTypes;
import com.logicore.shippingservice.application.command.MarkShippedCommand;
import com.logicore.shippingservice.application.dto.ShipmentResponse;
import com.logicore.shippingservice.application.port.out.ShipmentEventPublisher;
import com.logicore.shippingservice.application.port.out.ShipmentRepository;
import com.logicore.shippingservice.domain.exception.InvalidShipmentStatusTransitionException;
import com.logicore.shippingservice.domain.exception.ShipmentNotFoundException;
import com.logicore.shippingservice.domain.model.CustomerId;
import com.logicore.shippingservice.domain.model.OrderId;
import com.logicore.shippingservice.domain.model.Shipment;
import com.logicore.shippingservice.domain.model.ShipmentId;
import com.logicore.shippingservice.domain.model.ShipmentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarkShippedApplicationServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private ShipmentEventPublisher eventPublisher;

    @InjectMocks
    private MarkShippedApplicationService service;

    @Test
    void shipsCreatedShipmentAndPublishesShipmentShipped() {
        Shipment created = Shipment.create(OrderId.of(UUID.randomUUID()), CustomerId.of(UUID.randomUUID()));
        when(shipmentRepository.findById(created.id())).thenReturn(Optional.of(created));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(inv -> inv.getArgument(0));

        ShipmentResponse response = service.markShipped(new MarkShippedCommand(created.id()));

        assertThat(response.status()).isEqualTo(ShipmentStatus.SHIPPED);

        ArgumentCaptor<DomainEvent<?>> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().eventType()).isEqualTo(EventTypes.SHIPMENT_SHIPPED);
    }

    @Test
    void throwsWhenShipmentMissing() {
        ShipmentId id = ShipmentId.newId();
        when(shipmentRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.markShipped(new MarkShippedCommand(id)))
                .isInstanceOf(ShipmentNotFoundException.class);
    }

    @Test
    void cannotShipAnAlreadyShippedShipment() {
        Shipment shipped = Shipment.create(OrderId.of(UUID.randomUUID()), CustomerId.of(UUID.randomUUID()))
                .ship();
        when(shipmentRepository.findById(shipped.id())).thenReturn(Optional.of(shipped));

        assertThatThrownBy(() -> service.markShipped(new MarkShippedCommand(shipped.id())))
                .isInstanceOf(InvalidShipmentStatusTransitionException.class);
    }
}