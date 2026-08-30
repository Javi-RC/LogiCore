package com.logicore.userservice.application.port.in;

import com.logicore.userservice.application.command.RegisterUserCommand;
import com.logicore.userservice.application.dto.UserResponse;

/**
 * Inbound port: use case to register a new user.
 */
public interface RegisterUserUseCase {

    UserResponse register(RegisterUserCommand command);
}
