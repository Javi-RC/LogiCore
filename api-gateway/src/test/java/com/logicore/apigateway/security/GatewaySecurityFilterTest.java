package com.logicore.apigateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class GatewaySecurityFilterTest {

    private static final String SECRET = "logicore-dev-secret-change-me-in-production!";

    private GatewaySecurityFilter filter;

    @BeforeEach
    void setUp() {
        filter = new GatewaySecurityFilter(new JwtTokenParser(SECRET));
    }

    @Test
    void allowsPublicAuthPathsWithoutToken() {
        MockServerWebExchange exchange = exchange("/api/auth/login");
        AtomicReference<ServerWebExchange> downstream = new AtomicReference<>();
        GatewayFilterChain chain = e -> {
            downstream.set(e);
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
        assertThat(downstream.get()).isSameAs(exchange);
    }

    @Test
    void rejectsMissingTokenOnProtectedPath() {
        MockServerWebExchange exchange = exchange("/api/products/1");

        Mono<Void> result = filter.filter(exchange, exchange1 -> Mono.empty());

        StepVerifier.create(result).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getResponse().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    }

    @Test
    void rejectsInvalidTokenOnProtectedPath() {
        MockServerWebExchange exchange = exchange("/api/orders", "Bearer garbage");

        Mono<Void> result = filter.filter(exchange, exchange1 -> Mono.empty());

        StepVerifier.create(result).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void forwardsIdentityHeadersForValidToken() {
        AtomicReference<ServerWebExchange> downstream = new AtomicReference<>();
        GatewayFilterChain chain = exchange -> {
            downstream.set(exchange);
            return Mono.empty();
        };
        MockServerWebExchange exchange = exchange("/api/orders", "Bearer " + token(List.of("CUSTOMER", "ADMIN")));

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        HttpHeaders headers = downstream.get().getRequest().getHeaders();
        assertThat(headers.getFirst("X-User-Id")).isEqualTo("123e4567-e89b-12d3-a456-426614174000");
        assertThat(headers.getFirst("X-User-Email")).isEqualTo("jane@example.com");
        assertThat(headers.getFirst("X-User-Roles")).isEqualTo("CUSTOMER,ADMIN");
        assertThat(headers.containsKey(HttpHeaders.AUTHORIZATION)).isFalse();
    }

    private MockServerWebExchange exchange(String path) {
        return exchange(path, null);
    }

    private MockServerWebExchange exchange(String path, String authorization) {
        MockServerHttpRequest.BaseBuilder<?> builder = MockServerHttpRequest.get(path);
        if (authorization != null) {
            builder.header(HttpHeaders.AUTHORIZATION, authorization);
        }
        return MockServerWebExchange.from(builder.build());
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