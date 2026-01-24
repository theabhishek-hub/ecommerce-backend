package com.abhishek.ecommerce.security.config;

import com.abhishek.ecommerce.security.jwt.JwtAuthenticationFilter;
import com.abhishek.ecommerce.security.oauth2.OAuth2SuccessHandler;
import com.abhishek.ecommerce.security.authentication.FormLoginSuccessHandler;
import com.abhishek.ecommerce.security.exception.RestAccessDeniedHandler;
import com.abhishek.ecommerce.security.exception.RestAuthenticationEntryPoint;
import com.abhishek.ecommerce.security.filter.SellerRoleRefreshFilter;
import com.abhishek.ecommerce.security.logout.CustomLogoutSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final FormLoginSuccessHandler formLoginSuccessHandler;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;
    private final CorsConfigurationSource corsConfigurationSource;
    private final CustomLogoutSuccessHandler customLogoutSuccessHandler;
    private final SellerRoleRefreshFilter sellerRoleRefreshFilter;

    @Value("${config.security.oauth2.enabled:true}")
    private boolean oauth2Enabled;

    /**
     * SecurityFilterChain for REST APIs (JWT + Session-based)
     * Matches: /api/** requests
     * Authentication: JWT tokens in Authorization header OR session cookies
     * Session policy: IF_REQUIRED (allows both JWT and session)
     */
    @Bean("apiSecurityFilterChain")
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {

        http
                .securityMatcher(new AntPathRequestMatcher("/api/**"))
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/products/**",
                                "/api/v1/categories/**",
                                "/api/v1/brands/**",
                                "/api/v1/inventory/products/*/stock"
                        ).permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/**").authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler)
                );

        return http.build();
    }

    /**
     * SecurityFilterChain for UI Pages (Session-based, form login)
     * Matches: All non-API requests (/, /products, /cart, /checkout, etc.)
     * Authentication: Session cookies + form login
     * CSRF: Enabled with CookieCsrfTokenRepository
     */
    @Bean("uiSecurityFilterChain")
    public SecurityFilterChain uiSecurityFilterChain(HttpSecurity http) throws Exception {

        http
                .securityMatcher("/**")
                .csrf(csrf -> csrf.csrfTokenRepository(
                    CookieCsrfTokenRepository.withHttpOnlyFalse()
                ))
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - Note: /logout is handled by Spring Security, NOT as permitAll
                        .requestMatchers("/login", "/register").permitAll()
                        .requestMatchers(
                                "/",
                                "/products",
                                "/products-page/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico",
                                "/error/**"
                        ).permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers(oauth2Enabled ? "/oauth2/**" : "/oauth2-disabled/**").permitAll()
                        .requestMatchers(oauth2Enabled ? "/login/oauth2/**" : "/login-oauth2-disabled/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        // Admin UI pages - require ROLE_ADMIN
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // Seller application page - accessible to authenticated users (ROLE_USER)
                        .requestMatchers("/seller/apply").authenticated()
                        // Other seller UI pages - require ROLE_SELLER
                        .requestMatchers("/seller/**").hasRole("SELLER")
                        // Cart page - allow anonymous (uses localStorage), but checkout requires auth
                        .requestMatchers("/cart").permitAll()
                        // Protected endpoints - require session authentication
                        .requestMatchers("/checkout/**", "/orders/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(sellerRoleRefreshFilter, org.springframework.security.web.access.intercept.AuthorizationFilter.class)
                .formLogin(login -> login
                        .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(formLoginSuccessHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(customLogoutSuccessHandler)
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID", "access_token", "refresh_token")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
                );

        if (oauth2Enabled) {
            http.oauth2Login(oauth2 -> oauth2
                    .successHandler(oAuth2SuccessHandler)
            );
        }

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }




}
