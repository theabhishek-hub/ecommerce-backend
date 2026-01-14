package com.abhishek.ecommerce.security.jwt;

import com.abhishek.ecommerce.config.appProperties.JwtProperties;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collection;
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

    public String generateToken(String username, Collection<String> roles) {

        Date now = new Date();
        long expiryMs = jwtProperties.getAccessTokenExpiration();
        Date expiry = new Date(now.getTime() + expiryMs);

        JwtBuilder builder = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiry);

        // store roles as list and single role for backward compatibility
        if (roles != null && !roles.isEmpty()) {
            builder.claim("roles", roles.stream().toList());
            builder.claim("role", roles.iterator().next()); // First role for backward compatibility
        }

        return builder
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // =========================
    // Refresh Token
    public String generateRefreshToken(String username) {

        Date now = new Date();
        long expiryMs = jwtProperties.getRefreshTokenExpiration();
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
        return jwtProperties.getRefreshTokenExpiration() / 1000;
    }
}
