package com.logicore.userservice;

import com.logicore.userservice.application.command.RegisterUserCommand;
import com.logicore.userservice.application.dto.AuthenticationResponse;
import com.logicore.userservice.application.port.in.AuthenticateUseCase;
import com.logicore.userservice.application.port.in.RegisterUserUseCase;
import com.logicore.userservice.domain.exception.InvalidCredentialsException;
import com.logicore.userservice.domain.model.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class AuthenticationIT {

    @Container
    static PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("user_db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private RegisterUserUseCase registerUserUseCase;

    @Autowired
    private AuthenticateUseCase authenticateUseCase;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Test
    void registerThenLoginRoundTrip() {
        UUID userId = registerUserUseCase.register(new RegisterUserCommand(
                "jane@example.com", "s3cret!", "Jane Doe", UserRole.CUSTOMER)).id();

        AuthenticationResponse auth = authenticateUseCase.authenticate("jane@example.com", "s3cret!");

        assertThat(auth.token()).isNotBlank();
        assertThat(auth.tokenType()).isEqualTo("Bearer");
        assertThat(auth.expiresAt()).isAfter(java.time.Instant.now());

        Claims claims = parse(auth.token());
        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.get("roles", List.class)).containsExactly("CUSTOMER");
    }

    @Test
    void loginWithWrongPasswordIsRejected() {
        registerUserUseCase.register(new RegisterUserCommand(
                "bob@example.com", "correct!", "Bob", UserRole.ADMIN));

        assertThatThrownBy(() -> authenticateUseCase.authenticate("bob@example.com", "wrong"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    private Claims parse(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}