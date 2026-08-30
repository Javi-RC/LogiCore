package com.logicore.userservice.domain.exception;

/**
 * Thrown when attempting to register a user whose email already exists.
 */
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
