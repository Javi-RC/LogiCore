package com.logicore.userservice.adapter.in.web.error;

import java.time.Instant;

/**
 * Structured, uniform error response body returned by the API.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path
) {
}
