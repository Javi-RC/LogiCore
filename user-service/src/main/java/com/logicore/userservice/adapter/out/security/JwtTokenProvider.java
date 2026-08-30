package com.logicore.userservice.adapter.out.security;

import com.logicore.userservice.application.model.AuthenticatedUser;
import com.logicore.userservice.application.port.out.TokenProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider implements TokenProvider {

    private final SecretKey key;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String issue(AuthenticatedUser user, long ttlMillis) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.userId().toString())
                .claim("email", user.email())
                .claim("name", user.name())
                .claim("roles", List.of(user.role().name()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(ttlMillis)))
                .signWith(key)
                .compact();
    }
}