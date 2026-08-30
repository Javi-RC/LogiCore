package com.logicore.userservice.application.command;

import com.logicore.userservice.domain.model.UserRole;

/**
 * Immutable command object for the {@code RegisterUser} use case.
 */
public record RegisterUserCommand(
        String email,
        String password,
        String name,
        UserRole role
) {
}
