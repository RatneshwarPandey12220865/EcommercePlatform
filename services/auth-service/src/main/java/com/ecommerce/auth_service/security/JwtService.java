package com.ecommerce.auth_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey key;
    private final long accessTokenTtlMs;
    private final long refreshTokenTtlMs;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-ttl-minutes:15}") long accessTokenTtlMinutes,
            @Value("${jwt.refresh-token-ttl-days:7}") long refreshTokenTtlDays
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtlMs = accessTokenTtlMinutes * 60_000;
        this.refreshTokenTtlMs = refreshTokenTtlDays * 24 * 60 * 60_000;
    }

    public String generateAccessToken(String subject, String role) {
        return buildToken(subject, Map.of("role", role, "type", "access"), accessTokenTtlMs);
    }

    public String generateRefreshToken(String subject) {
        return buildToken(subject, Map.of("type", "refresh"), refreshTokenTtlMs);
    }

    private String buildToken(String subject, Map<String, Object> claims, long ttlMs) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(ttlMs)))
                .signWith(key)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractSubject(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public long accessTokenTtlSeconds() {
        return accessTokenTtlMs / 1000;
    }

    public long refreshTokenTtlSeconds() {
        return refreshTokenTtlMs / 1000;
    }
}
