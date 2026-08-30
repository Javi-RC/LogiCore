package com.logicore.userservice.domain.model;

import java.util.UUID;

/**
 * Value object identifying a user. Immutable and equality-based.
 */
public record UserId(UUID value) {

    public UserId {
        if (value == null) {
            throw new IllegalArgumentException("User id must not be null");
        }
    }

    public static UserId of(UUID value) {
        return new UserId(value);
    }

    public static UserId newId() {
        return new UserId(UUID.randomUUID());
    }
}
