package com.logicore.orderservice.application.port.in;

import com.logicore.orderservice.application.dto.OrderResponse;
import com.logicore.orderservice.domain.model.OrderId;

/**
 * Inbound port: use case to cancel a pending/confirmed order.
 */
public interface CancelOrderUseCase {

    OrderResponse cancel(OrderId orderId);
}