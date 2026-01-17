package com.abhishek.ecommerce.security.authentication;

import com.abhishek.ecommerce.shared.enums.Role;
import com.abhishek.ecommerce.shared.enums.SellerStatus;
import com.abhishek.ecommerce.seller.repository.SellerRepository;
import com.abhishek.ecommerce.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Form Login Success Handler for Thymeleaf UI authentication.
 * 
 * Redirects users based on their role and seller status:
 * - ADMIN                      -> /admin/dashboard
 * - SELLER (APPROVED)          -> /seller/dashboard
 * - SELLER (PENDING/REQUESTED) -> /seller/apply
 * - USER                        -> /
 * 
 * This handles Spring Security form login (not OAuth2).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FormLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        String email = authentication.getName();
        log.debug("Form login success for user: {}", email);

        try {
            // CRITICAL: Fetch fresh user data from database to get latest roles and seller status
            // This ensures admin approval changes are reflected immediately on login
            var user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalStateException("User not found: " + email));

            // CRITICAL: Update SecurityContext with latest roles from database
            // This ensures navbar and controllers see the correct roles
            Collection<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(role.name()))
                    .collect(Collectors.toList());
            
            UsernamePasswordAuthenticationToken updatedAuth = new UsernamePasswordAuthenticationToken(
                    user.getEmail(),
                    authentication.getCredentials(),
                    authorities
            );
            SecurityContextHolder.getContext().setAuthentication(updatedAuth);
            log.debug("Updated SecurityContext with fresh roles for user: {}", email);

            Set<String> roleNames = user.getRoles().stream()
                    .map(Role::name)
                    .collect(Collectors.toSet());

            log.debug("User {} has roles: {} (fresh from DB)", email, roleNames);

            // Determine redirect URL based on roles and seller status (from seller profile)
            String redirectUrl = determineRedirectUrl(user, roleNames);
            
            log.info("Form login redirect for user {} to {}", email, redirectUrl);
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            log.error("Error in form login success handler for user: {}", email, e);
            response.sendRedirect("/");
        }
    }

    /**
     * Determine redirect URL based on user's roles and seller status.
     */
    private String determineRedirectUrl(com.abhishek.ecommerce.user.entity.User user, Set<String> roleNames) {
        // Priority 1: Check if ADMIN
        if (roleNames.contains(Role.ROLE_ADMIN.name())) {
            log.debug("User {} is ADMIN, redirecting to admin dashboard", user.getEmail());
            return "/admin";
        }

        // Priority 2: Check if SELLER
        if (roleNames.contains(Role.ROLE_SELLER.name())) {
            // Check seller approval status
            return sellerRepository.findByUserId(user.getId())
                    .map(seller -> {
                        switch (seller.getStatus()) {
                            case APPROVED:
                                log.debug("Seller {} is APPROVED, redirecting to seller dashboard", user.getEmail());
                                return "/seller/dashboard";
                            case REQUESTED:
                            case SUSPENDED:
                            case REJECTED:
                                log.debug("Seller {} has status {}, redirecting to seller apply", user.getEmail(), seller.getStatus());
                                return "/seller/apply";
                            default:
                                return "/";
                        }
                    })
                    .orElse("/");
        }

        // Default: Regular USER
        log.debug("User {} is regular USER, redirecting to home", user.getEmail());
        return "/";
    }
}
