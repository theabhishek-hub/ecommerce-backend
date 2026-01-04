package com.abhishek.ecommerce.config.security;

import com.abhishek.ecommerce.user.entity.User;
import com.abhishek.ecommerce.user.entity.UserStatus;
import com.abhishek.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2User oauthUser = ((OAuth2AuthenticationToken) authentication).getPrincipal();
            String email = oauthUser.getAttribute("email");

            // create user if not present
            User user = userRepository.findByEmail(email).orElseGet(() -> {
                User u = new User();
                u.setEmail(email);
                u.setFullName(oauthUser.getAttribute("name"));
                u.setStatus(UserStatus.ACTIVE);
                u.setRole("ROLE_USER");
                u.setAuthProvider("GOOGLE");
                // no password for OAuth accounts
                u.setPasswordHash("");
                return userRepository.save(u);
            });

            // generate JWT
            String token = jwtUtil.generateToken(user.getEmail());

            // Return token in response body (simple JSON)
            response.setContentType("application/json");
            response.getWriter().write("{\"token\":\"" + token + "\"}");
        }
    }
}

