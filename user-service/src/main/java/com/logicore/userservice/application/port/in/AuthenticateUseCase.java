package com.logicore.userservice.application.port.in;

import com.logicore.userservice.application.dto.AuthenticationResponse;

public interface AuthenticateUseCase {

    AuthenticationResponse authenticate(String email, String rawPassword);
}