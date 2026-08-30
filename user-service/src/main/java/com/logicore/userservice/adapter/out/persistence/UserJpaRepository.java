package com.logicore.userservice.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository over {@link UserJpaEntity}. This is the low-level persistence
 * primitive; the domain-facing {@link com.logicore.userservice.application.port.out.UserRepository}
 * port is implemented by {@link UserPersistenceAdapter} on top of it.
 */
@Repository
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {

    Optional<UserJpaEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
