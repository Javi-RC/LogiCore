package com.logicore.userservice.application.port.out;

import com.logicore.userservice.domain.model.User;
import com.logicore.userservice.domain.model.UserId;

import java.util.Optional;

/**
 * Outbound port for persisting and retrieving {@link User}s. Implemented by an adapter
 * (JPA) outside the application core.
 */
public interface UserRepository {

    User save(User user);

    Optional<User> findById(UserId id);

    Optional<User> findByEmail(String email);
}
