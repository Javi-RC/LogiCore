package com.logicore.shippingservice.adapter.in.web.error;

import com.logicore.shippingservice.domain.exception.InvalidShipmentStatusTransitionException;
import com.logicore.shippingservice.domain.exception.ShipmentNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Centralized exception handling for the shipment API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ShipmentNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ShipmentNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "SHIPMENT_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidShipmentStatusTransitionException.class)
    public ResponseEntity<ApiError> handleInvalidTransition(InvalidShipmentStatusTransitionException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "INVALID_SHIPMENT_STATUS_TRANSITION", ex.getMessage(), request);
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