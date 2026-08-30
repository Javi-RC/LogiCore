package com.logicore.userservice.application.service;

import com.logicore.userservice.application.dto.AuthenticationResponse;
import com.logicore.userservice.application.dto.UserResponse;
import com.logicore.userservice.application.model.AuthenticatedUser;
import com.logicore.userservice.application.port.in.AuthenticateUseCase;
import com.logicore.userservice.application.port.out.PasswordEncoder;
import com.logicore.userservice.application.port.out.TokenProvider;
import com.logicore.userservice.application.port.out.UserRepository;
import com.logicore.userservice.domain.exception.InvalidCredentialsException;
import com.logicore.userservice.domain.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthenticateApplicationService implements AuthenticateUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final long tokenTtlMillis;

    public AuthenticateApplicationService(UserRepository userRepository,
                                          PasswordEncoder passwordEncoder,
                                          TokenProvider tokenProvider,
                                          @Value("${jwt.expiration-ms}") long tokenTtlMillis) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.tokenTtlMillis = tokenTtlMillis;
    }

    @Override
    public AuthenticationResponse authenticate(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(rawPassword, user.passwordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = tokenProvider.issue(
                new AuthenticatedUser(user.id().value(), user.email(), user.name(), user.role()),
                tokenTtlMillis);
        Instant expiresAt = Instant.now().plusMillis(tokenTtlMillis);
        return AuthenticationResponse.of(token, expiresAt, UserResponse.from(user));
    }
}