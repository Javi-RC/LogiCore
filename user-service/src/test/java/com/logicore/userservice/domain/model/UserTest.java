package com.logicore.userservice.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    @Test
    void createsValidUser() {
        User user = User.create(
                UserId.newId(),
                "test@example.com",
                "hashed-password",
                "Test User",
                UserRole.CUSTOMER
        );

        assertThat(user.email()).isEqualTo("test@example.com");
        assertThat(user.role()).isEqualTo(UserRole.CUSTOMER);
        assertThat(user.createdAt()).isNotNull();
    }

    @Test
    void rejectsBlankEmail() {
        assertThatThrownBy(() -> User.create(UserId.newId(), "  ", "hash", "Name", UserRole.CUSTOMER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email");
    }

    @Test
    void rejectsBlankPasswordHash() {
        assertThatThrownBy(() -> User.create(UserId.newId(), "a@b.com", "", "Name", UserRole.CUSTOMER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("password");
    }

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> User.create(UserId.newId(), "a@b.com", "hash", "  ", UserRole.CUSTOMER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name");
    }

    @Test
    void rejectsNullRole() {
        assertThatThrownBy(() -> User.create(UserId.newId(), "a@b.com", "hash", "Name", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("role");
    }

    @Test
    void rehydratePreservesValues() {
        UserId id = UserId.of(java.util.UUID.randomUUID());
        User user = User.rehydrate(id, "a@b.com", "hash", "Name", UserRole.ADMIN, java.time.Instant.EPOCH);

        assertThat(user.id()).isEqualTo(id);
        assertThat(user.role()).isEqualTo(UserRole.ADMIN);
        assertThat(user.createdAt()).isEqualTo(java.time.Instant.EPOCH);
    }
}
