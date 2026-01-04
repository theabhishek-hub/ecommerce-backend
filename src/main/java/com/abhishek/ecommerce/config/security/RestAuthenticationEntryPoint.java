package com.abhishek.ecommerce.config.security;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final SecurityEventLogger securityEventLogger;

    @Override
    public void commence(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, AuthenticationException authException) throws IOException {
        // Log unauthorized access attempt
        String reason = authException.getMessage() != null ? authException.getMessage() : "Authentication required";
        securityEventLogger.logUnauthorizedAccess(request, reason);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        String json = String.format("{\"success\":false,\"status\":%d,\"message\":\"%s\",\"data\":null}", 
                HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        response.getWriter().write(json);
    }
}
