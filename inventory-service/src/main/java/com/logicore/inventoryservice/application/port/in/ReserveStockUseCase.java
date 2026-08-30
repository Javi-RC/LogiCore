package com.logicore.inventoryservice.application.port.in;

import com.logicore.inventoryservice.application.command.ReserveStockCommand;
import com.logicore.inventoryservice.application.dto.InventoryItemResponse;

/**
 * Inbound port: reserve stock for an order.
 */
public interface ReserveStockUseCase {

    InventoryItemResponse reserve(ReserveStockCommand command);
}