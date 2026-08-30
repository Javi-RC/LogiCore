package com.logicore.inventoryservice.application.port.in;

import com.logicore.inventoryservice.application.command.ReleaseStockCommand;

/**
 * Inbound port: release previously reserved stock (compensation for cancelled/failed orders).
 */
public interface ReleaseStockUseCase {

    void release(ReleaseStockCommand command);
}