package com.abhishek.ecommerce.config.security;

import com.abhishek.ecommerce.config.appProperties.JwtProperties;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final JwtProperties jwtProperties;

    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    // =========================
    // Signing Key (use accessSecret)
    // =========================
    private Key getSigningKey() {
        String secret = jwtProperties.getAccessSecret();
        byte[] keyBytes = (secret != null ? secret : "").getBytes(StandardCharsets.UTF_8);

        return Keys.hmacShaKeyFor(keyBytes);
    }

    // =========================
    // Access Token

    public String generateToken(String username, String role) {

        Date now = new Date();
        long expiryMs = jwtProperties.getAccessTokenExpirationMs();
        Date expiry = new Date(now.getTime() + expiryMs);

        JwtBuilder builder = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiry);

        // keep role claim exactly as before
        if (role != null && !role.isBlank()) {
            builder.claim("role", role);
        }

        return builder
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // =========================
    // Refresh Token
    public String generateRefreshToken(String username) {

        Date now = new Date();
        long expiryMs = jwtProperties.getRefreshTokenExpirationMs();
        Date expiry = new Date(now.getTime() + expiryMs);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiry)
                // keep your existing refresh marker
                .claim("type", "refresh")
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }


    public long getRefreshExpirationSeconds() {
        return jwtProperties.getRefreshTokenExpirationMs() / 1000;
    }
}
