package com.logicore.userservice.domain.model;

import java.time.Instant;

/**
 * Domain aggregate representing a user account.
 *
 * <p>This is the domain model and must remain independent of any framework or persistence
 * concern. The persisted password here is already the result of a strong hash (see the
 * {@code PasswordEncoder} outbound port); plain-text passwords never enter the domain.</p>
 */
public class User {

    private final UserId id;
    private final String email;
    private final String passwordHash;
    private final String name;
    private final UserRole role;
    private final Instant createdAt;

    private User(UserId id, String email, String passwordHash, String name, UserRole role, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.role = role;
        this.createdAt = createdAt;
    }

    /**
     * Factory used when creating a new user. Validates basic invariants.
     */
    public static User create(UserId id, String email, String passwordHash, String name, UserRole role) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email must not be blank");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("password hash must not be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (role == null) {
            throw new IllegalArgumentException("role must not be null");
        }
        return new User(id, email, passwordHash, name, role, Instant.now());
    }

    /**
     * Factory used to rebuild a user from persistence (no re-validation of invariants is needed).
     */
    public static User rehydrate(UserId id, String email, String passwordHash, String name, UserRole role, Instant createdAt) {
        return new User(id, email, passwordHash, name, role, createdAt);
    }

    public UserId id() {
        return id;
    }

    public String email() {
        return email;
    }

    public String passwordHash() {
        return passwordHash;
    }

    public String name() {
        return name;
    }

    public UserRole role() {
        return role;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
