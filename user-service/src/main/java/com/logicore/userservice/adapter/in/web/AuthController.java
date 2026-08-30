package com.logicore.userservice.adapter.in.web;

import com.logicore.userservice.adapter.in.web.dto.LoginRequest;
import com.logicore.userservice.application.dto.AuthenticationResponse;
import com.logicore.userservice.application.port.in.AuthenticateUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticateUseCase authenticateUseCase;

    public AuthController(AuthenticateUseCase authenticateUseCase) {
        this.authenticateUseCase = authenticateUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticateUseCase.authenticate(request.email(), request.password()));
    }
}