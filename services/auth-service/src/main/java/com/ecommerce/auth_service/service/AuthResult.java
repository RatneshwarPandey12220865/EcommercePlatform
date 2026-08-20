package com.ecommerce.auth_service.service;

import com.ecommerce.auth_service.dto.AuthResponse;

/**
 * Internal to the service layer - carries the raw tokens alongside the public
 * response body so the controller can set them as httpOnly cookies without
 * the tokens ever appearing in a JSON response.
 */
public record AuthResult(AuthResponse body, String accessToken, String refreshToken) {
}
