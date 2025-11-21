package com.rudraksha.authservice.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey secretKey;

    @Value("${jwt.access.expiration:900000}")   // 15 min = 900,000 ms
    private long accessExpiration;

    @Value("${jwt.refresh.expiration:2592000000}") // 30 days = 2,592,000,000 ms
    private long refreshExpiration;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        byte[] decodedKey = Base64.getDecoder()
                .decode(secret.getBytes(StandardCharsets.UTF_8));
        this.secretKey = Keys.hmacShaKeyFor(decodedKey);
    }

    // ==========================================
    // GENERATE ACCESS TOKEN (15 min)
    // ==========================================
    public String generateAccessToken(String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .claim("type", "ACCESS")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpiration))
                .signWith(secretKey)
                .compact();
    }

    // ==========================================
    // GENERATE REFRESH TOKEN (30 days)
    // ==========================================
    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .subject(email)
                .claim("type", "REFRESH")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(secretKey)
                .compact();
    }

    // ==========================================
    // VALIDATION
    // ==========================================
    public void validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
        } catch (JwtException e) {
            throw new JwtException("Invalid Token: " + e.getMessage());
        }
    }

    // ==========================================
    // EXTRACTION
    // ==========================================
    public String extractUsername(String token) {
        return Jwts.parser().verifyWith(secretKey)
                .build().parseSignedClaims(token)
                .getPayload().getSubject();
    }

    public String extractRole(String token) {
        return (String) Jwts.parser().verifyWith(secretKey)
                .build().parseSignedClaims(token)
                .getPayload().get("role");
    }

    public String extractType(String token) {
        return (String) Jwts.parser().verifyWith(secretKey)
                .build().parseSignedClaims(token)
                .getPayload().get("type");
    }
}
