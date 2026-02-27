package com.example.demo.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT Service for generating, validating, and parsing JWT tokens.
 *
 * FIX (Critical C3): The previous getSigningKey() was calling
 * Base64.getEncoder().encode() on a plain-text secret string, which
 * double-encodes the key and can silently produce a weak or incorrect key.
 * The correct approach: treat jwt.secret as a Base64-encoded value and
 * DECODE it to obtain the raw key bytes.
 *
 * If the secret in application.properties is NOT already Base64, use the
 * UTF-8 bytes directly (as done here with the fallback) — but in
 * production the secret MUST be a Base64-encoded random 256-bit value.
 */
@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Generate a JWT token for the given username without extra claims.
     */
    public String generateToken(String username) {
        return generateToken(new HashMap<>(), username);
    }

    /**
     * Generate a JWT token with additional claims (e.g., role).
     */
    public String generateToken(Map<String, Object> extraClaims, String username) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract the username (subject) from a JWT token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Validate a JWT token against a username:
     * - checks the subject matches
     * - checks the token is not expired
     * Returns false (and logs) for any JWT parsing exception.
     */
    public boolean isTokenValid(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            return extractedUsername.equals(username) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * FIX (C3): Correctly derive the signing key.
     *
     * The jwt.secret property should be a Base64-encoded 256-bit (32-byte) value.
     * We DECODE it (not encode) to get raw bytes, then build an HMAC-SHA-256 key.
     *
     * If the value is not valid Base64 we fall back to raw UTF-8 bytes so the
     * app doesn't crash during development, but a warning is logged.
     */
    private Key getSigningKey() {
        try {
            byte[] keyBytes = java.util.Base64.getDecoder().decode(secretKey);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException ex) {
            // Fallback for plain-text secrets (local dev only)
            logger.warn("jwt.secret is not valid Base64 — using raw UTF-8 bytes. "
                    + "For production, use a Base64-encoded 256-bit key.");
            return Keys.hmacShaKeyFor(secretKey.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }
}
