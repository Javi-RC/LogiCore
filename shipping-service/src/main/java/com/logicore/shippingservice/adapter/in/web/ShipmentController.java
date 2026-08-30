package com.logicore.shippingservice.adapter.in.web;

import com.logicore.shippingservice.application.command.CreateShipmentCommand;
import com.logicore.shippingservice.application.command.MarkDeliveredCommand;
import com.logicore.shippingservice.application.command.MarkShippedCommand;
import com.logicore.shippingservice.application.dto.ShipmentResponse;
import com.logicore.shippingservice.application.port.in.CreateShipmentUseCase;
import com.logicore.shippingservice.application.port.in.GetShipmentUseCase;
import com.logicore.shippingservice.application.port.in.MarkDeliveredUseCase;
import com.logicore.shippingservice.application.port.in.MarkShippedUseCase;
import com.logicore.shippingservice.domain.exception.ShipmentNotFoundException;
import com.logicore.shippingservice.domain.model.CustomerId;
import com.logicore.shippingservice.domain.model.OrderId;
import com.logicore.shippingservice.domain.model.ShipmentId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST inbound adapter for shipment operations.
 */
@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final CreateShipmentUseCase createShipmentUseCase;
    private final MarkShippedUseCase markShippedUseCase;
    private final MarkDeliveredUseCase markDeliveredUseCase;
    private final GetShipmentUseCase getShipmentUseCase;

    public ShipmentController(CreateShipmentUseCase createShipmentUseCase,
                              MarkShippedUseCase markShippedUseCase,
                              MarkDeliveredUseCase markDeliveredUseCase,
                              GetShipmentUseCase getShipmentUseCase) {
        this.createShipmentUseCase = createShipmentUseCase;
        this.markShippedUseCase = markShippedUseCase;
        this.markDeliveredUseCase = markDeliveredUseCase;
        this.getShipmentUseCase = getShipmentUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShipmentResponse create(@Valid @RequestBody CreateShipmentRequest request) {
        return createShipmentUseCase.createShipment(new CreateShipmentCommand(
                OrderId.of(request.orderId()), CustomerId.of(request.customerId())));
    }

    @GetMapping
    public List<ShipmentResponse> getAll() {
        return getShipmentUseCase.getAll();
    }

    @GetMapping("/{id}")
    public ShipmentResponse getById(@PathVariable UUID id) {
        return getShipmentUseCase.getById(ShipmentId.of(id))
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment " + id + " not found"));
    }

    @GetMapping("/order/{orderId}")
    public ShipmentResponse getByOrderId(@PathVariable UUID orderId) {
        return getShipmentUseCase.getByOrderId(OrderId.of(orderId))
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment for order " + orderId + " not found"));
    }

    @PostMapping("/{id}/ship")
    public ShipmentResponse ship(@PathVariable UUID id) {
        return markShippedUseCase.markShipped(new MarkShippedCommand(ShipmentId.of(id)));
    }

    @PostMapping("/{id}/deliver")
    public ShipmentResponse deliver(@PathVariable UUID id) {
        return markDeliveredUseCase.markDelivered(new MarkDeliveredCommand(ShipmentId.of(id)));
    }
}