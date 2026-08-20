package com.ecommerce.auth_service.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String email,
        String role
) {
}
