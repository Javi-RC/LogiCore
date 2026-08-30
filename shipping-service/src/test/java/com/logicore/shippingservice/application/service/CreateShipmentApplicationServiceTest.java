package com.logicore.shippingservice.application.service;

import com.logicore.common.DomainEvent;
import com.logicore.common.event.EventTypes;
import com.logicore.shippingservice.application.command.CreateShipmentCommand;
import com.logicore.shippingservice.application.dto.ShipmentResponse;
import com.logicore.shippingservice.application.port.out.ShipmentEventPublisher;
import com.logicore.shippingservice.application.port.out.ShipmentRepository;
import com.logicore.shippingservice.domain.model.CustomerId;
import com.logicore.shippingservice.domain.model.OrderId;
import com.logicore.shippingservice.domain.model.Shipment;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateShipmentApplicationServiceTest {

    private static final OrderId ORDER = OrderId.of(UUID.randomUUID());
    private static final CustomerId CUSTOMER = CustomerId.of(UUID.randomUUID());

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private ShipmentEventPublisher eventPublisher;

    @InjectMocks
    private CreateShipmentApplicationService service;

    @Test
    void createsShipmentAndPublishesShipmentCreated() {
        when(shipmentRepository.findByOrderId(ORDER)).thenReturn(Optional.empty());
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(inv -> inv.getArgument(0));

        ShipmentResponse response = service.createShipment(new CreateShipmentCommand(ORDER, CUSTOMER));

        assertThat(response.status()).isEqualTo(ShipmentStatus.CREATED);
        assertThat(response.orderId()).isEqualTo(ORDER.value());

        ArgumentCaptor<DomainEvent<?>> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().eventType()).isEqualTo(EventTypes.SHIPMENT_CREATED);
    }

    @Test
    void doesNotDuplicateShipmentForSameOrder() {
        Shipment existing = Shipment.create(ORDER, CUSTOMER);
        when(shipmentRepository.findByOrderId(ORDER)).thenReturn(Optional.of(existing));

        service.createShipment(new CreateShipmentCommand(ORDER, CUSTOMER));

        verify(shipmentRepository, times(0)).save(any());
    }
}