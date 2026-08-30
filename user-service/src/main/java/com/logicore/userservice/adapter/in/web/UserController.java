package com.logicore.userservice.adapter.in.web;

import com.logicore.userservice.adapter.in.web.dto.RegisterUserRequest;
import com.logicore.userservice.application.command.RegisterUserCommand;
import com.logicore.userservice.application.dto.UserResponse;
import com.logicore.userservice.application.port.in.GetUserUseCase;
import com.logicore.userservice.application.port.in.RegisterUserUseCase;
import com.logicore.userservice.domain.model.UserId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

/**
 * Inbound adapter exposing the user endpoints over HTTP. Contains no business rules;
 * it translates HTTP into use-case calls and back.
 */
@RestController
@RequestMapping("/api")
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;
    private final GetUserUseCase getUserUseCase;

    public UserController(RegisterUserUseCase registerUserUseCase, GetUserUseCase getUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.getUserUseCase = getUserUseCase;
    }

    @PostMapping("/auth/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        RegisterUserCommand command = new RegisterUserCommand(
                request.email(),
                request.password(),
                request.name(),
                request.role()
        );
        UserResponse created = registerUserUseCase.register(command);
        return ResponseEntity
                .created(URI.create("/api/users/" + created.id()))
                .body(created);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        return getUserUseCase.getUser(UserId.of(id))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
