package com.logicore.orderservice.adapter.in.web.error;

import java.time.Instant;

/**
 * Structured, uniform error response body returned by the order API.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path
) {
}