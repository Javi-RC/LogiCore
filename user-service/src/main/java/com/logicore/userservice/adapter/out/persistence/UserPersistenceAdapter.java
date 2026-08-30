package com.logicore.userservice.adapter.out.persistence;

import com.logicore.userservice.application.port.out.UserRepository;
import com.logicore.userservice.domain.model.User;
import com.logicore.userservice.domain.model.UserId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Outbound adapter implementing {@link UserRepository} backed by Spring Data JPA.
 * Converts domain objects to/from JPA entities and delegates to the Spring Data repository.
 */
@Component
public class UserPersistenceAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final UserPersistenceMapper mapper;

    public UserPersistenceAdapter(UserJpaRepository jpaRepository, UserPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public User save(User user) {
        UserJpaEntity saved = jpaRepository.save(mapper.toEntity(user));
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(UserId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(mapper::toDomain);
    }
}
