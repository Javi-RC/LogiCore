package com.logicore.apigateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenParserTest {

    private static final String SECRET = "logicore-dev-secret-change-me-in-production!";

    private final JwtTokenParser parser = new JwtTokenParser(SECRET);

    @Test
    void parsesValidToken() {
        String token = token(List.of("ADMIN"));

        var claims = parser.parse(token);

        assertThat(claims).isPresent();
        assertThat(claims.get().getSubject()).isEqualTo("123e4567-e89b-12d3-a456-426614174000");
        assertThat(claims.get().get("email")).isEqualTo("jane@example.com");
        assertThat(claims.get().get("roles", List.class)).containsExactly("ADMIN");
    }

    @Test
    void rejectsGarbageToken() {
        assertThat(parser.parse("not-a-jwt")).isEmpty();
    }

    @Test
    void rejectsTokenSignedWithDifferentSecret() {
        SecretKey otherKey = Keys.hmacShaKeyFor("another-secret-at-least-32-bytes-long!!".getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("123e4567-e89b-12d3-a456-426614174000")
                .claim("roles", List.of("ADMIN"))
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(otherKey)
                .compact();

        assertThat(parser.parse(token)).isEmpty();
    }

    @Test
    void rejectsExpiredToken() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("123e4567-e89b-12d3-a456-426614174000")
                .claim("roles", List.of("CUSTOMER"))
                .issuedAt(Date.from(Instant.now().minusSeconds(7200)))
                .expiration(Date.from(Instant.now().minusSeconds(3600)))
                .signWith(key)
                .compact();

        assertThat(parser.parse(token)).isEmpty();
    }

    private String token(List<String> roles) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject("123e4567-e89b-12d3-a456-426614174000")
                .claim("email", "jane@example.com")
                .claim("roles", roles)
                .issuedAt(Date.from(Instant.now().minusSeconds(60)))
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(key)
                .compact();
    }
}