package com.abhishek.ecommerce.config.security;

import com.abhishek.ecommerce.config.appProperties.AppProperties;
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

    private final AppProperties appProperties;

    public JwtUtil(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    // =========================
    // Signing Key
    // =========================
    private Key getSigningKey() {
        // NO @Value
        // NO default secret
        // NO base64 try/catch guessing
        byte[] keyBytes = appProperties.getJwtSecret()
                .getBytes(StandardCharsets.UTF_8);

        return Keys.hmacShaKeyFor(keyBytes);
    }

    // =========================
    // Access Token

    public String generateToken(String username, String role) {

        Date now = new Date();
        Date expiry = new Date(
                now.getTime() + appProperties.getExpirationMs()
        );

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
        Date expiry = new Date(
                now.getTime() + appProperties.getRefreshExpirationMs()
        );

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
        return appProperties.getRefreshExpirationMs() / 1000;
    }
}
