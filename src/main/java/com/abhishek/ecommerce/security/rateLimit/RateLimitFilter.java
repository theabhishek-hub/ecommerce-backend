package com.abhishek.ecommerce.security.rateLimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

/**
 * Simple Redis-backed fixed-window rate limiter for auth endpoints.
 * - Limits: 10 requests per minute per client IP
 * - Active only when `spring.cache.type=redis` so tests (test profile) remain unaffected.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class RateLimitFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;

    private static final int LIMIT = 10;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        if (!requestURI.equals("/api/v1/auth/login") && !requestURI.equals("/api/v1/auth/refresh")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIP = getClientIP(request);
        String key = "rl:auth:" + clientIP + ":" + (System.currentTimeMillis() / WINDOW.toMillis());

        Long current = redisTemplate.opsForValue().increment(key, 1);
        if (current != null && current == 1L) {
            redisTemplate.expire(key, WINDOW);
        }

        if (current != null && current <= LIMIT) {
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(LIMIT - current));
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(WINDOW.getSeconds()));
            response.getWriter().write("Too many requests");
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}