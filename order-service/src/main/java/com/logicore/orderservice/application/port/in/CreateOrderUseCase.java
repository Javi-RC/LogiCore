package com.logicore.orderservice.application.port.in;

import com.logicore.orderservice.application.command.CreateOrderCommand;
import com.logicore.orderservice.application.dto.OrderResponse;

/**
 * Inbound port: use case to create a new order.
 */
public interface CreateOrderUseCase {

    OrderResponse createOrder(CreateOrderCommand command);
}