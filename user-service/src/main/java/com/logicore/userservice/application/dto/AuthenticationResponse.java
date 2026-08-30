package com.logicore.userservice.application.dto;

import java.time.Instant;

public record AuthenticationResponse(String token, String tokenType, Instant expiresAt, UserResponse user) {

    public static AuthenticationResponse of(String token, Instant expiresAt, UserResponse user) {
        return new AuthenticationResponse(token, "Bearer", expiresAt, user);
    }
}