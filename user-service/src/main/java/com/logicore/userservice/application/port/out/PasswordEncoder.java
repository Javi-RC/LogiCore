package com.logicore.userservice.application.port.out;

/**
 * Outbound port for hashing and verifying passwords. Implemented by an adapter using a
 * strong algorithm (e.g. BCrypt via Spring Security).
 */
public interface PasswordEncoder {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
