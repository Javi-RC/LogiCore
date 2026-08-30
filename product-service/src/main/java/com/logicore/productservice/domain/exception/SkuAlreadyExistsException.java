package com.logicore.productservice.domain.exception;

public class SkuAlreadyExistsException extends RuntimeException {

    public SkuAlreadyExistsException(String message) {
        super(message);
    }
}
