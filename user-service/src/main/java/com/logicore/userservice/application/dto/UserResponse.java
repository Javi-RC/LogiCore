package com.logicore.userservice.application.dto;

import com.logicore.userservice.domain.model.User;
import com.logicore.userservice.domain.model.UserRole;

import java.time.Instant;
import java.util.UUID;

/**
 * API response DTO for a user. Never exposes the password hash.
 */
public record UserResponse(
        UUID id,
        String email,
        String name,
        UserRole role,
        Instant createdAt
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.id().value(),
                user.email(),
                user.name(),
                user.role(),
                user.createdAt()
        );
    }
}
