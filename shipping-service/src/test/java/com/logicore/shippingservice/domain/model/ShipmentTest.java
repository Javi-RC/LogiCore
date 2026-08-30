package com.logicore.shippingservice.domain.model;

import com.logicore.shippingservice.domain.exception.InvalidShipmentStatusTransitionException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShipmentTest {

    private static final OrderId ORDER = OrderId.of(UUID.randomUUID());
    private static final CustomerId CUSTOMER = CustomerId.of(UUID.randomUUID());

    @Test
    void createShipmentStartsInCreatedState() {
        Shipment shipment = Shipment.create(ORDER, CUSTOMER);
        assertThat(shipment.status()).isEqualTo(ShipmentStatus.CREATED);
        assertThat(shipment.orderId()).isEqualTo(ORDER);
        assertThat(shipment.customerId()).isEqualTo(CUSTOMER);
        assertThat(shipment.id()).isNotNull();
        assertThat(shipment.createdAt()).isNotNull();
    }

    @Test
    void shipMovesCreatedToShipped() {
        Shipment shipped = Shipment.create(ORDER, CUSTOMER).ship();
        assertThat(shipped.status()).isEqualTo(ShipmentStatus.SHIPPED);
    }

    @Test
    void deliverMovesShippedToDelivered() {
        Shipment delivered = Shipment.create(ORDER, CUSTOMER).ship().deliver();
        assertThat(delivered.status()).isEqualTo(ShipmentStatus.DELIVERED);
    }

    @Test
    void cannotDeliverBeforeShipping() {
        assertThatThrownBy(() -> Shipment.create(ORDER, CUSTOMER).deliver())
                .isInstanceOf(InvalidShipmentStatusTransitionException.class)
                .hasMessageContaining("cannot deliver");
    }

    @Test
    void cannotShipTwice() {
        Shipment shipped = Shipment.create(ORDER, CUSTOMER).ship();
        assertThatThrownBy(shipped::ship)
                .isInstanceOf(InvalidShipmentStatusTransitionException.class)
                .hasMessageContaining("cannot ship");
    }

    @Test
    void stateChangesAreImmutable() {
        Shipment created = Shipment.create(ORDER, CUSTOMER);
        created.ship();
        assertThat(created.status()).isEqualTo(ShipmentStatus.CREATED);
    }
}