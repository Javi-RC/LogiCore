package com.logicore.userservice.adapter.in.web.dto;

import com.logicore.userservice.domain.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for registering a new user. Bean Validation enforces request invariants.
 */
public record RegisterUserRequest(
        @NotBlank(message = "email is required")
        @Email(message = "email must be a valid address")
        String email,

        @NotBlank(message = "password is required")
        @Size(min = 8, max = 72, message = "password must be between 8 and 72 characters")
        String password,

        @NotBlank(message = "name is required")
        String name,

        @NotNull(message = "role is required")
        UserRole role
) {
}
