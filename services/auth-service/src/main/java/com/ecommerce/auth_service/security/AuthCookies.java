package com.ecommerce.auth_service.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class AuthCookies {

    public static final String ACCESS_TOKEN_COOKIE = "access_token";
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private final boolean secure;

    public AuthCookies(@Value("${jwt.cookie-secure:false}") boolean secure) {
        this.secure = secure;
    }

    public ResponseCookie accessTokenCookie(String token, long maxAgeSeconds) {
        return build(ACCESS_TOKEN_COOKIE, token, maxAgeSeconds);
    }

    public ResponseCookie refreshTokenCookie(String token, long maxAgeSeconds) {
        return build(REFRESH_TOKEN_COOKIE, token, maxAgeSeconds);
    }

    public ResponseCookie clearedAccessTokenCookie() {
        return build(ACCESS_TOKEN_COOKIE, "", 0);
    }

    public ResponseCookie clearedRefreshTokenCookie() {
        return build(REFRESH_TOKEN_COOKIE, "", 0);
    }

    private ResponseCookie build(String name, String value, long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }
}
