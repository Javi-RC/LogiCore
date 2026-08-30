package com.logicore.userservice.domain.model;

/**
 * Roles a {@link User} can have. Used for authorization (e.g. administrative endpoints).
 */
public enum UserRole {
    CUSTOMER,
    ADMIN
}
