package com.logicore.userservice.adapter.out.persistence;

import com.logicore.userservice.domain.model.User;
import com.logicore.userservice.domain.model.UserId;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Maps between the domain {@link User} and the persistence {@link UserJpaEntity}.
 * This mapping lives in the persistence adapter to keep the domain free of Hibernate.
 */
@Component
public class UserPersistenceMapper {

    public UserJpaEntity toEntity(User user) {
        return new UserJpaEntity(
                user.id().value(),
                user.email(),
                user.passwordHash(),
                user.name(),
                user.role(),
                user.createdAt()
        );
    }

    public User toDomain(UserJpaEntity entity) {
        return User.rehydrate(
                UserId.of(entity.getId()),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getName(),
                entity.getRole(),
                entity.getCreatedAt()
        );
    }
}
