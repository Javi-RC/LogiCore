package com.logicore.userservice.application.service;

import com.logicore.userservice.application.dto.AuthenticationResponse;
import com.logicore.userservice.application.port.out.PasswordEncoder;
import com.logicore.userservice.application.port.out.TokenProvider;
import com.logicore.userservice.application.port.out.UserRepository;
import com.logicore.userservice.domain.exception.InvalidCredentialsException;
import com.logicore.userservice.domain.model.User;
import com.logicore.userservice.domain.model.UserId;
import com.logicore.userservice.domain.model.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticateApplicationServiceTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String EMAIL = "jane@example.com";
    private static final String PASSWORD = "s3cret";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenProvider tokenProvider;

    @Test
    void authenticateIssuesTokenForValidCredentials() {
        User user = User.rehydrate(UserId.of(USER_ID), EMAIL, "hashed", "Jane", UserRole.ADMIN, Instant.now());
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(PASSWORD, "hashed")).thenReturn(true);
        when(tokenProvider.issue(org.mockito.ArgumentMatchers.any(), anyLong())).thenReturn("jwt-token");

        AuthenticateApplicationService service = newService();
        AuthenticationResponse response = service.authenticate(EMAIL, PASSWORD);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresAt()).isAfter(Instant.now());
        assertThat(response.user().id()).isEqualTo(USER_ID);
        assertThat(response.user().role()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void authenticateRejectsUnknownEmail() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        AuthenticateApplicationService service = newService();
        assertThatThrownBy(() -> service.authenticate(EMAIL, PASSWORD))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(passwordEncoder, never()).matches(eq(PASSWORD), org.mockito.ArgumentMatchers.any());
        verify(tokenProvider, never()).issue(org.mockito.ArgumentMatchers.any(), anyLong());
    }

    @Test
    void authenticateRejectsWrongPassword() {
        User user = User.rehydrate(UserId.of(USER_ID), EMAIL, "hashed", "Jane", UserRole.CUSTOMER, Instant.now());
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(PASSWORD, "hashed")).thenReturn(false);

        AuthenticateApplicationService service = newService();
        assertThatThrownBy(() -> service.authenticate(EMAIL, PASSWORD))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(tokenProvider, never()).issue(org.mockito.ArgumentMatchers.any(), anyLong());
    }

    private AuthenticateApplicationService newService() {
        return new AuthenticateApplicationService(userRepository, passwordEncoder, tokenProvider, 3_600_000L);
    }
}