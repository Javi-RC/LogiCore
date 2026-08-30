package com.logicore.userservice.application.port.in;

import com.logicore.userservice.application.dto.UserResponse;
import com.logicore.userservice.domain.model.UserId;

import java.util.Optional;

/**
 * Inbound port: use case to retrieve a user.
 */
public interface GetUserUseCase {

    Optional<UserResponse> getUser(UserId id);
}
