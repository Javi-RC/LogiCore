package com.logicore.productservice.adapter.in.web;

import com.logicore.productservice.adapter.in.web.dto.CreateProductRequest;
import com.logicore.productservice.adapter.in.web.dto.UpdateProductRequest;
import com.logicore.productservice.application.command.CreateProductCommand;
import com.logicore.productservice.application.command.UpdateProductCommand;
import com.logicore.productservice.application.dto.ProductResponse;
import com.logicore.productservice.application.port.in.CreateProductUseCase;
import com.logicore.productservice.application.port.in.DeleteProductUseCase;
import com.logicore.productservice.application.port.in.GetProductUseCase;
import com.logicore.productservice.application.port.in.UpdateProductUseCase;
import com.logicore.productservice.domain.model.ProductId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Inbound adapter exposing product endpoints over HTTP. No business rules here.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final GetProductUseCase getProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;

    public ProductController(CreateProductUseCase createProductUseCase,
                             GetProductUseCase getProductUseCase,
                             UpdateProductUseCase updateProductUseCase,
                             DeleteProductUseCase deleteProductUseCase) {
        this.createProductUseCase = createProductUseCase;
        this.getProductUseCase = getProductUseCase;
        this.updateProductUseCase = updateProductUseCase;
        this.deleteProductUseCase = deleteProductUseCase;
    }

    @GetMapping
    public List<ProductResponse> getProducts() {
        return getProductUseCase.getProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable UUID id) {
        return getProductUseCase.getProduct(ProductId.of(id))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse created = createProductUseCase.create(new CreateProductCommand(
                request.sku(), request.name(), request.description(), request.price()
        ));
        return ResponseEntity.created(URI.create("/api/products/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateProductRequest request) {
        ProductResponse updated = updateProductUseCase.update(new UpdateProductCommand(
                ProductId.of(id), request.name(), request.description(), request.price()
        ));
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteProductUseCase.delete(ProductId.of(id));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ProductResponse> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(updateProductUseCase.deactivate(ProductId.of(id)));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ProductResponse> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(updateProductUseCase.activate(ProductId.of(id)));
    }
}
