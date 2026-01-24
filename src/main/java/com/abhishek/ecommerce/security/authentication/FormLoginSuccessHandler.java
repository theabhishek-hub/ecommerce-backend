package com.abhishek.ecommerce.security.authentication;

import com.abhishek.ecommerce.shared.enums.Role;
import com.abhishek.ecommerce.shared.enums.SellerStatus;
import com.abhishek.ecommerce.user.repository.UserRepository;
import com.abhishek.ecommerce.security.userDetails.CustomUserDetailsService;
import com.abhishek.ecommerce.security.jwt.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
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
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

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

            log.debug("DIAGNOSTIC - User {} loaded from DB with roles: {}", email, user.getRoles());
            log.info("🔍 DIAGNOSTIC - User {} roles count: {}, roles: {}", email, 
                    user.getRoles() != null ? user.getRoles().size() : 0, 
                    user.getRoles());

            // CRITICAL: Reload UserDetails with fresh roles from database
            // This ensures the principal has the latest roles (including ROLE_SELLER if approved)
            var userDetails = userDetailsService.loadUserByUsername(email);
            
            // Extract fresh authorities from UserDetails (includes latest roles from DB)
            Collection<SimpleGrantedAuthority> authorities = userDetails.getAuthorities().stream()
                    .map(auth -> new SimpleGrantedAuthority(auth.getAuthority()))
                    .collect(Collectors.toList());
            
            log.debug("DIAGNOSTIC - Creating authorities for user {}: {}", email, authorities);
            log.info("🔍 AUTHORITIES FOR {}: {}", email, authorities);
            
            // CRITICAL: Build roleNames from fresh UserDetails authorities (not from old user object)
            // This ensures we have the latest roles from the database
            Set<String> roleNames = authorities.stream()
                    .map(auth -> auth.getAuthority())
                    .collect(Collectors.toSet());

            log.debug("User {} has roles: {} (fresh from UserDetails)", email, roleNames);
            log.info("🔍 ROLENAMES FOR {}: {}", email, roleNames);
            
            // CRITICAL: Reload user from database one more time to ensure we have the latest seller status
            // This is important because the user might have been approved between the first load and now
            user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalStateException("User not found: " + email));
            
            log.debug("DIAGNOSTIC - Reloaded user {} from DB - sellerStatus: {}, roles: {}", 
                    email, user.getSellerStatus(), user.getRoles());
            log.info("🔍 RELOADED USER - Email: {}, SellerStatus: {}, Roles: {}", 
                    email, user.getSellerStatus(), user.getRoles());
            
            // Create new authentication with UserDetails principal and fresh authorities
            // Using UserDetails ensures full compatibility with Spring Security
            UsernamePasswordAuthenticationToken updatedAuth = new UsernamePasswordAuthenticationToken(
                    userDetails, // Use fresh UserDetails with updated roles
                    authentication.getCredentials(),
                    authorities // Fresh authorities from database
            );
            
            // CRITICAL: Set authentication in SecurityContext
            // Spring Security will automatically save this to the session
            SecurityContextHolder.getContext().setAuthentication(updatedAuth);
            
            log.debug("Updated SecurityContext with fresh roles for user: {}", email);
            log.info("✅ SecurityContext updated with UserDetails for user: {} (roles: {})", email, authorities);

            // Determine redirect URL based on roles and seller status (from fresh user object)
            String redirectUrl = determineRedirectUrl(user, roleNames);
            
            // Generate JWT token and set it as a cookie for API authentication
            String jwtToken = jwtUtil.generateToken(email, roleNames);
            Cookie accessTokenCookie = new Cookie("access_token", jwtToken);
            accessTokenCookie.setHttpOnly(false); // Allow JavaScript to read it
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
            response.addCookie(accessTokenCookie);
            
            log.info("Form login redirect for user {} to {} (JWT token issued)", email, redirectUrl);
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
        log.debug("DIAGNOSTIC - determineRedirectUrl for user: {}", user.getEmail());
        log.debug("DIAGNOSTIC - User roles from authentication: {}", roleNames);
        log.info("🔍 DETERMINE_REDIRECT - User: {}, RoleNames: {}", user.getEmail(), roleNames);
        
        // Priority 1: Check if ADMIN
        if (roleNames.contains(Role.ROLE_ADMIN.name())) {
            log.debug("User {} is ADMIN, redirecting to admin dashboard", user.getEmail());
            log.info("✅ ADMIN DETECTED - Redirecting to /admin");
            return "/admin";
        }

        // Priority 2: Check seller status first (even if ROLE_SELLER not in session yet)
        // This handles the case where user was approved but session hasn't been refreshed
        SellerStatus sellerStatus = user.getSellerStatus();
        if (sellerStatus == null) {
            sellerStatus = SellerStatus.NOT_A_SELLER;
        }
        
        log.info("🔍 Checking seller status: {} for user {}", sellerStatus, user.getEmail());
        log.info("🔍 Checking if ROLE_SELLER in roles: {}", roleNames.contains(Role.ROLE_SELLER.name()));
        
        // If user is APPROVED in database, redirect to dashboard (even if ROLE_SELLER not in session yet)
        // The filter will refresh the session when they access /seller/dashboard
        if (SellerStatus.APPROVED.equals(sellerStatus)) {
            if (roleNames.contains(Role.ROLE_SELLER.name())) {
                log.info("✅ Seller {} is APPROVED with ROLE_SELLER, redirecting to seller dashboard", user.getEmail());
                log.info("✅ REDIRECT DECISION: /seller/dashboard (APPROVED seller with ROLE_SELLER)");
                return "/seller/dashboard";
            } else {
                log.warn("⚠️ Seller {} is APPROVED in DB but ROLE_SELLER not in session - redirecting to dashboard anyway", user.getEmail());
                log.warn("⚠️ Filter will refresh session when accessing /seller/dashboard");
                log.info("✅ REDIRECT DECISION: /seller/dashboard (APPROVED seller, will refresh session)");
                return "/seller/dashboard";
            }
        }
        
        // Check if SELLER (for other statuses)
        if (roleNames.contains(Role.ROLE_SELLER.name())) {
            log.debug("DIAGNOSTIC - User {} has ROLE_SELLER, checking seller status from DB", user.getEmail());
            log.info("✅ SELLER ROLE FOUND - Checking seller status");
            
            log.debug("DIAGNOSTIC - User {} seller status from DB: {} (roles: {})", user.getEmail(), sellerStatus, user.getRoles());
            log.info("🔍 SELLER STATUS: {} for user {}", sellerStatus, user.getEmail());
            
            switch (sellerStatus) {
                case REQUESTED:
                    log.warn("⚠️ Seller {} has ROLE_SELLER but status is REQUESTED (not APPROVED) - redirecting to apply page", user.getEmail());
                    log.warn("⚠️ REDIRECT DECISION: /seller/apply (status is REQUESTED, not APPROVED)");
                    log.warn("⚠️ DIAGNOSTIC: User has ROLE_SELLER but sellerStatus={} - this suggests approval didn't complete", sellerStatus);
                    return "/seller/apply";
                case NOT_A_SELLER:
                    log.warn("⚠️ User {} has ROLE_SELLER but NOT_A_SELLER status - this should not happen", user.getEmail());
                    log.warn("⚠️ REDIRECT DECISION: / (inconsistent state)");
                    return "/";
                case SUSPENDED:
                    log.warn("❌ Seller {} is SUSPENDED, redirecting to home", user.getEmail());
                    return "/";
                case REJECTED:
                    log.warn("❌ Seller {} is REJECTED, redirecting to home", user.getEmail());
                    return "/";
                default:
                    log.error("❌ Unknown seller status {} for user {}", sellerStatus, user.getEmail());
                    return "/";
            }
        } else {
            log.debug("DIAGNOSTIC - User {} does NOT have ROLE_SELLER. Available roles: {}", user.getEmail(), roleNames);
            log.info("🔍 NO ROLE_SELLER - Available roles: {}", roleNames);
            
            // If status is REQUESTED but no ROLE_SELLER, redirect to apply page
            if (SellerStatus.REQUESTED.equals(sellerStatus)) {
                log.info("⏳ User {} has REQUESTED status (pending approval), redirecting to seller apply", user.getEmail());
                return "/seller/apply";
            }
        }

        // Default: Regular USER
        log.debug("User {} is regular USER (no ROLE_SELLER), redirecting to home", user.getEmail());
        log.info("➡️ DEFAULT USER - Redirecting to /");
        return "/";
    }
}
