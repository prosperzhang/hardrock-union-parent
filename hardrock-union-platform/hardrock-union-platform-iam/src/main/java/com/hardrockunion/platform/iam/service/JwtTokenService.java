package com.hardrockunion.platform.iam.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtTokenService {

    private final SecretKey secretKey;
    private final long expireSeconds;

    public JwtTokenService(
        @Value("${hardrock.security.jwt-secret:hardrock-union-secret-hardrock-union-secret}") String jwtSecret,
        @Value("${hardrock.security.jwt-expire-seconds:7200}") long expireSeconds
    ) {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.expireSeconds = expireSeconds;
    }

    public String createToken(Long userId, String appCode, Long tenantId, String username) {
        Instant now = Instant.now();
        Instant expireAt = now.plusSeconds(expireSeconds);
        io.jsonwebtoken.JwtBuilder builder = Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("appCode", appCode)
            .claim("username", username)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expireAt));
        if (tenantId != null) {
            builder = builder.claim("tenantId", tenantId);
        }
        return builder.signWith(secretKey).compact();
    }

    public long getExpireSeconds() {
        return expireSeconds;
    }
}
