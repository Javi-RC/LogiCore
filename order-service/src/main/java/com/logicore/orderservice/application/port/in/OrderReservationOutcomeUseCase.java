package com.logicore.orderservice.application.port.in;

import com.logicore.orderservice.application.dto.OrderResponse;
import com.logicore.orderservice.domain.model.OrderId;

/**
 * Inbound port: internal use case invoked when inventory reservation outcomes arrive
 * (StockReserved / StockReservationFailed) via the Kafka consumer adapter.
 */
public interface OrderReservationOutcomeUseCase {

    OrderResponse onStockReserved(OrderId orderId);

    OrderResponse onStockReservationFailed(OrderId orderId);
}