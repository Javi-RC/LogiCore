package com.logicore.inventoryservice.application.port.in;

import com.logicore.inventoryservice.application.command.RegisterInventoryCommand;
import com.logicore.inventoryservice.application.dto.InventoryItemResponse;

/**
 * Inbound port: register a new inventory item for a product.
 */
public interface RegisterStockUseCase {

    InventoryItemResponse registerStock(RegisterInventoryCommand command);
}