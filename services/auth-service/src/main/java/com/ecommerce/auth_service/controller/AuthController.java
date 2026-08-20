package com.ecommerce.auth_service.controller;

import com.ecommerce.auth_service.dto.AuthResponse;
import com.ecommerce.auth_service.dto.LoginRequest;
import com.ecommerce.auth_service.dto.RegisterRequest;
import com.ecommerce.auth_service.security.AuthCookies;
import com.ecommerce.auth_service.security.JwtService;
import com.ecommerce.auth_service.service.AuthResult;
import com.ecommerce.auth_service.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthCookies authCookies;
    private final JwtService jwtService;

    public AuthController(AuthService authService, AuthCookies authCookies, JwtService jwtService) {
        this.authService = authService;
        this.authCookies = authCookies;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResult result = authService.register(request);
        return withAuthCookies(result, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResult result = authService.login(request);
        return withAuthCookies(result, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, authCookies.clearedAccessTokenCookie().toString())
                .header(HttpHeaders.SET_COOKIE, authCookies.clearedRefreshTokenCookie().toString())
                .build();
    }

    private ResponseEntity<AuthResponse> withAuthCookies(AuthResult result, HttpStatus status) {
        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, authCookies.accessTokenCookie(result.accessToken(), jwtService.accessTokenTtlSeconds()).toString())
                .header(HttpHeaders.SET_COOKIE, authCookies.refreshTokenCookie(result.refreshToken(), jwtService.refreshTokenTtlSeconds()).toString())
                .body(result.body());
    }
}
