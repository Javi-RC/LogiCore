package com.logicore.userservice.application.model;

import com.logicore.userservice.domain.model.UserRole;

import java.util.UUID;

public record AuthenticatedUser(UUID userId, String email, String name, UserRole role) {
}