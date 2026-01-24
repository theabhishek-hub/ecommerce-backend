package com.abhishek.ecommerce.security.oauth2;

import com.abhishek.ecommerce.shared.enums.AuthProvider;
import com.abhishek.ecommerce.shared.enums.Role;
import com.abhishek.ecommerce.shared.enums.SellerStatus;
import com.abhishek.ecommerce.security.jwt.JwtUtil;
import com.abhishek.ecommerce.user.entity.User;
import com.abhishek.ecommerce.shared.enums.UserStatus;
import com.abhishek.ecommerce.user.repository.UserRepository;
import com.abhishek.ecommerce.auth.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * OAuth2 Success Handler for Thymeleaf UI-based authentication.
 * 
 * This handler:
 * 1. Creates or retrieves user from database
 * 2. Generates JWT access + refresh tokens
 * 3. Stores tokens in HttpOnly, Secure cookies
 * 4. Redirects users to role-appropriate pages
 * 
 * Role-based redirect logic:
 * - ADMIN -> /admin/dashboard
 * - SELLER (APPROVED) -> /seller/dashboard
 * - SELLER (PENDING/REQUESTED) -> /seller/apply
 * - USER -> /
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauth2User = authToken.getPrincipal();

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        // Validation
        if (email == null || email.isBlank()) {
            log.error("OAuth2: Email not found in OAuth2 user attributes");
            response.sendRedirect("/login?error=invalid_oauth_user");
            return;
        }

        try {
            // 1. Create or retrieve user
            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setEmail(email);
                        newUser.setFullName(name);
                        newUser.setRoles(Set.of(Role.ROLE_USER));
                        newUser.setProvider(AuthProvider.GOOGLE);
                        newUser.setStatus(UserStatus.ACTIVE);
                        return userRepository.save(newUser);
                    });

            // Ensure user has at least ROLE_USER
            if (user.getRoles() == null || user.getRoles().isEmpty()) {
                user.setRoles(Set.of(Role.ROLE_USER));
                user = userRepository.save(user);
            }

            log.info("OAuth2 login successful for user: {}", email);

            // 2. Generate JWT tokens
            String accessToken = jwtUtil.generateToken(
                    user.getEmail(),
                    user.getRoles().stream().map(Role::name).toList()
            );

            com.abhishek.ecommerce.auth.entity.RefreshToken refreshTokenEntity =
                    refreshTokenService.createOrReplaceRefreshToken(user);
            String refreshToken = refreshTokenEntity.getToken();

            // 3. Store tokens in HttpOnly cookies
            storeTokensInCookies(response, accessToken, refreshToken);

            // 3.5. Set authentication with roles in SecurityContext for navbar rendering
            Collection<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(role.name()))
                    .collect(Collectors.toList());
            
            UsernamePasswordAuthenticationToken usernamePasswordAuthToken = new UsernamePasswordAuthenticationToken(
                    user.getEmail(), 
                    null, 
                    authorities
            );
            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthToken);
            log.debug("Set authentication with roles for OAuth2 user: {}", user.getEmail());

            // 4. Determine redirect URL based on role and seller status
            String redirectUrl = determineRedirectUrl(user);

            log.info("OAuth2 user {} redirected to {}", email, redirectUrl);
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            log.error("OAuth2 authentication error: {}", e.getMessage(), e);
            response.sendRedirect("/login?error=authentication_failed");
        }
    }

    /**
     * Store JWT tokens in HttpOnly, Secure cookies for browser-based Thymeleaf UI.
     * This ensures tokens are secure and automatically sent with requests.
     */
    private void storeTokensInCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        // Access Token Cookie (short-lived)
        Cookie accessTokenCookie = new Cookie("access_token", accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true); // HTTPS only in production
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(900); // 15 minutes
        response.addCookie(accessTokenCookie);

        // Refresh Token Cookie (long-lived)
        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(604800); // 7 days
        response.addCookie(refreshTokenCookie);

        log.debug("JWT tokens stored in HttpOnly cookies");
    }

    /**
     * Determine redirect URL based on user role and seller status.
     * 
     * Redirect logic:
     * - ADMIN -> Admin dashboard
     * - SELLER with APPROVED status -> Seller dashboard
     * - SELLER with PENDING/REQUESTED status -> Seller application page
     * - USER -> Home page
     */
    private String determineRedirectUrl(User user) {
        // Check if user is ADMIN
        if (user.getRoles().contains(Role.ROLE_ADMIN)) {
            return "/admin/dashboard";
        }

        // Check if user is SELLER
        if (user.getRoles().contains(Role.ROLE_SELLER)) {
            // Check seller status directly from User entity
            SellerStatus sellerStatus = user.getSellerStatus();
            if (sellerStatus == null) {
                sellerStatus = SellerStatus.NOT_A_SELLER;
            }
            switch (sellerStatus) {
                case APPROVED:
                    log.debug("Seller {} has APPROVED status, redirecting to dashboard", user.getEmail());
                    return "/seller/dashboard";
                case REQUESTED:
                    log.debug("Seller {} has REQUESTED status, redirecting to apply page", user.getEmail());
                    return "/seller/apply";
                case NOT_A_SELLER:
                    log.debug("Seller {} has NOT_A_SELLER status, redirecting to apply page", user.getEmail());
                    return "/seller/apply";
                case REJECTED:
                case SUSPENDED:
                    log.warn("Seller {} has {} status, redirecting to home", user.getEmail(), sellerStatus);
                    return "/";
                default:
                    return "/";
            }
        }

        // Default: Regular USER -> Home
        return "/";
    }
}





