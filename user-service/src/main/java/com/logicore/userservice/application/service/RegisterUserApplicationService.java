package com.logicore.userservice.application.service;

import com.logicore.userservice.application.command.RegisterUserCommand;
import com.logicore.userservice.application.dto.UserResponse;
import com.logicore.userservice.application.port.in.RegisterUserUseCase;
import com.logicore.userservice.application.port.out.PasswordEncoder;
import com.logicore.userservice.application.port.out.UserRepository;
import com.logicore.userservice.domain.exception.EmailAlreadyExistsException;
import com.logicore.userservice.domain.model.User;
import com.logicore.userservice.domain.model.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service orchestrating the {@code RegisterUser} use case.
 * Coordinates the domain, password encoding, and persistence via ports.
 */
@Service
public class RegisterUserApplicationService implements RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUserApplicationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserResponse register(RegisterUserCommand command) {
        userRepository.findByEmail(command.email())
                .ifPresent(u -> {
                    throw new EmailAlreadyExistsException("A user with email " + command.email() + " already exists");
                });

        String passwordHash = passwordEncoder.encode(command.password());
        User user = User.create(UserId.newId(), command.email(), passwordHash, command.name(), command.role());
        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }
}
