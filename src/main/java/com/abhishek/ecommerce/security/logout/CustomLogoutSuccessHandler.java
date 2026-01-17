package com.abhishek.ecommerce.security.logout;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom logout success handler that clears JWT cookies (access_token and refresh_token)
 * that were set during OAuth2 login or API login.
 */
@Component
@Slf4j
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException {
        
        log.info("User logged out successfully");
        
        // Clear JWT cookies if they exist (for OAuth2 users)
        clearCookie(response, "access_token");
        clearCookie(response, "refresh_token");
        
        // Redirect to home page
        response.sendRedirect("/");
    }

    /**
     * Clear a cookie by setting its max age to 0
     */
    private void clearCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // Match the secure flag used when setting the cookie
        response.addCookie(cookie);
        log.debug("Cleared cookie: {}", cookieName);
    }
}
