package com.logicore.orderservice.adapter.in.web;

import com.logicore.orderservice.adapter.in.web.dto.CreateOrderRequest;
import com.logicore.orderservice.application.command.CreateOrderCommand;
import com.logicore.orderservice.application.dto.OrderResponse;
import com.logicore.orderservice.application.port.in.CancelOrderUseCase;
import com.logicore.orderservice.application.port.in.CreateOrderUseCase;
import com.logicore.orderservice.application.port.in.GetOrderUseCase;
import com.logicore.orderservice.domain.model.OrderId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Inbound adapter exposing order endpoints over HTTP.
 */
@RestController
@RequestMapping("/api")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;

    public OrderController(CreateOrderUseCase createOrderUseCase,
                           GetOrderUseCase getOrderUseCase,
                           CancelOrderUseCase cancelOrderUseCase) {
        this.createOrderUseCase = createOrderUseCase;
        this.getOrderUseCase = getOrderUseCase;
        this.cancelOrderUseCase = cancelOrderUseCase;
    }

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        CreateOrderCommand command = new CreateOrderCommand(
                request.customerId(),
                request.items().stream()
                        .map(item -> new CreateOrderCommand.Item(item.productId(), item.quantity()))
                        .toList()
        );
        OrderResponse created = createOrderUseCase.createOrder(command);
        return ResponseEntity.created(URI.create("/api/orders/" + created.id())).body(created);
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID id) {
        return getOrderUseCase.getOrder(OrderId.of(id))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/orders")
    public List<OrderResponse> getOrders(@RequestParam(required = false) UUID customerId) {
        if (customerId != null) {
            return getOrderUseCase.getOrdersByCustomer(customerId);
        }
        return getOrderUseCase.getOrders();
    }

    @PostMapping("/orders/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(cancelOrderUseCase.cancel(OrderId.of(id)));
    }
}