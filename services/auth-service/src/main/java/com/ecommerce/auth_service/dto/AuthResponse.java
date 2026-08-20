package com.ecommerce.auth_service.dto;

/**
 * Deliberately excludes the access/refresh tokens - those are set as httpOnly
 * cookies (see AuthController) and must never be readable by JS, to keep them
 * out of reach of an XSS payload.
 */
public record AuthResponse(
        String email,
        String role
) {
}
