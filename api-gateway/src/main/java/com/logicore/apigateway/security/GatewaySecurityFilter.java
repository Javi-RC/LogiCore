package com.logicore.apigateway.security;

import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Component
public class GatewaySecurityFilter implements GlobalFilter, Ordered {

    private static final List<String> PUBLIC_PATHS = List.of("/api/auth/", "/actuator/");
    private static final String BEARER_PREFIX = "Bearer ";
    private static final byte[] UNAUTHORIZED_BODY =
            "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Missing or invalid token\"}"
                    .getBytes(StandardCharsets.UTF_8);

    private final JwtTokenParser tokenParser;

    public GatewaySecurityFilter(JwtTokenParser tokenParser) {
        this.tokenParser = tokenParser;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (isPublic(exchange.getRequest().getURI().getPath())) {
            return chain.filter(exchange);
        }

        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return unauthorized(exchange);
        }

        Optional<Claims> claims = tokenParser.parse(authorization.substring(BEARER_PREFIX.length()));
        if (claims.isEmpty()) {
            return unauthorized(exchange);
        }

        ServerWebExchange mutated = forwardsIdentity(exchange, claims.get());
        return chain.filter(mutated);
    }

    private ServerWebExchange forwardsIdentity(ServerWebExchange exchange, Claims claims) {
        List<String> roles = claims.get("roles", List.class);
        String rolesJoined = roles == null ? "" : String.join(",", roles);
        return exchange.mutate()
                .request(request -> request.headers(headers -> {
                    headers.remove(HttpHeaders.AUTHORIZATION);
                    headers.set("X-User-Id", claims.getSubject());
                    headers.set("X-User-Email", String.valueOf(claims.get("email")));
                    headers.set("X-User-Roles", rolesJoined);
                }))
                .build();
    }

    private boolean isPublic(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(UNAUTHORIZED_BODY)));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}