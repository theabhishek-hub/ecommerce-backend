package com.abhishek.ecommerce.config.security;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final SecurityEventLogger securityEventLogger;

    @Override
    public void handle(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        // Log forbidden access attempt
        String reason = accessDeniedException.getMessage() != null ? accessDeniedException.getMessage() : "Insufficient permissions";
        securityEventLogger.logForbiddenAccess(request, reason);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        
        String json = String.format("{\"success\":false,\"status\":%d,\"message\":\"%s\",\"data\":null}", 
                HttpServletResponse.SC_FORBIDDEN, "Access Denied");
        response.getWriter().write(json);
    }
}
