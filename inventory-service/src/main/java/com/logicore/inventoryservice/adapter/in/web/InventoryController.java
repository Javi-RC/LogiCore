package com.logicore.inventoryservice.adapter.in.web;

import com.logicore.inventoryservice.application.command.RegisterInventoryCommand;
import com.logicore.inventoryservice.application.command.ReleaseStockCommand;
import com.logicore.inventoryservice.application.command.ReserveStockCommand;
import com.logicore.inventoryservice.application.dto.InventoryItemResponse;
import com.logicore.inventoryservice.application.port.in.GetStockUseCase;
import com.logicore.inventoryservice.application.port.in.RegisterStockUseCase;
import com.logicore.inventoryservice.application.port.in.ReleaseStockUseCase;
import com.logicore.inventoryservice.application.port.in.ReserveStockUseCase;
import com.logicore.inventoryservice.domain.exception.InventoryItemNotFoundException;
import com.logicore.inventoryservice.domain.model.ProductId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST inbound adapter for inventory operations.
 */
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final RegisterStockUseCase registerStockUseCase;
    private final ReserveStockUseCase reserveStockUseCase;
    private final ReleaseStockUseCase releaseStockUseCase;
    private final GetStockUseCase getStockUseCase;

    public InventoryController(RegisterStockUseCase registerStockUseCase,
                               ReserveStockUseCase reserveStockUseCase,
                               ReleaseStockUseCase releaseStockUseCase,
                               GetStockUseCase getStockUseCase) {
        this.registerStockUseCase = registerStockUseCase;
        this.reserveStockUseCase = reserveStockUseCase;
        this.releaseStockUseCase = releaseStockUseCase;
        this.getStockUseCase = getStockUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryItemResponse registerStock(@Valid @RequestBody RegisterStockRequest request) {
        return registerStockUseCase.registerStock(
                new RegisterInventoryCommand(ProductId.of(request.productId()), request.quantity()));
    }

    @GetMapping("/{productId}")
    public InventoryItemResponse getStock(@PathVariable UUID productId) {
        return getStockUseCase.getStock(ProductId.of(productId))
                .orElseThrow(() -> new InventoryItemNotFoundException("No inventory item for product " + productId));
    }

    @PostMapping("/{productId}/reserve")
    public InventoryItemResponse reserve(@PathVariable UUID productId,
                                         @Valid @RequestBody StockOperationRequest request) {
        return reserveStockUseCase.reserve(
                new ReserveStockCommand(request.correlationId(), ProductId.of(productId), request.quantity()));
    }

    @PostMapping("/{productId}/release")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void release(@PathVariable UUID productId,
                        @Valid @RequestBody StockOperationRequest request) {
        releaseStockUseCase.release(
                new ReleaseStockCommand(request.correlationId(), ProductId.of(productId), request.quantity()));
    }
}