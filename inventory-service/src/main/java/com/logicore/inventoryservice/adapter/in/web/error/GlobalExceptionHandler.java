package com.logicore.inventoryservice.adapter.in.web.error;

import com.logicore.inventoryservice.domain.exception.InsufficientStockException;
import com.logicore.inventoryservice.domain.exception.InventoryItemNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.StaleObjectStateException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Centralized exception handling for the inventory API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InventoryItemNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(InventoryItemNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "INVENTORY_ITEM_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiError> handleInsufficientStock(InsufficientStockException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "INSUFFICIENT_STOCK", ex.getMessage(), request);
    }

    @ExceptionHandler({OptimisticLockingFailureException.class,
            ObjectOptimisticLockingFailureException.class,
            StaleObjectStateException.class})
    public ResponseEntity<ApiError> handleConcurrentModification(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "CONCURRENT_MODIFICATION",
                "The inventory item was modified concurrently. Retry the operation.", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred", request);
    }

    private String formatFieldError(FieldError fe) {
        return fe.getField() + ": " + fe.getDefaultMessage();
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String code, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ApiError(
                Instant.now(), status.value(), code, message, request.getRequestURI()));
    }
}