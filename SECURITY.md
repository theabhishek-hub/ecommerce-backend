# SECURITY - Security Guidelines & Best Practices

## 🔒 Security Overview

This document outlines all security measures implemented in the AbhiOnlineDukaan backend and best practices for maintaining security.

---

## 🔐 Authentication & Authorization

### JWT Authentication

**Implementation:**
- **Algorithm**: HS512 (HMAC with SHA-512)
- **Access Token**: 15 minutes expiration
- **Refresh Token**: 7 days expiration
- **Storage**: Refresh tokens stored in database with user association

**Token Generation:**
```java
public String generateAccessToken(User user) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + accessTokenExpiration);
    
    return Jwts.builder()
        .setSubject(String.valueOf(user.getId()))
        .claim("email", user.getEmail())
        .claim("roles", user.getRoles())
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .signWith(SignatureAlgorithm.HS512, secretKey)
        .compact();
}
```

**Token Validation:**
```java
public boolean validateToken(String token) {
    try {
        Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token);
        return true;
    } catch (ExpiredJwtException e) {
        logger.error("JWT token expired: {}", e.getMessage());
        return false;
    } catch (JwtException e) {
        logger.error("Invalid JWT: {}", e.getMessage());
        return false;
    }
}
```

### OAuth2 Integration (Google Sign-In)

**Implementation:**
1. User clicks "Sign with Google"
2. Redirected to Google OAuth2 endpoint
3. User authenticates with Google
4. Google redirects back with authorization code
5. Backend exchanges code for tokens
6. Backend fetches user profile
7. User created/updated in database
8. JWT tokens returned to client

**Security Measures:**
- ✅ HTTPS enforced (Redirect URI)
- ✅ State parameter validation (CSRF protection)
- ✅ Client secret never exposed to frontend
- ✅ Code exchanged server-side only
- ✅ User profile verified before creating account

**Configuration:**
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            redirect-uri: https://yourdomain.com/auth/oauth2/callback
            scope: openid,profile,email
```

### Role-Based Access Control (RBAC)

**Roles Defined:**
```java
public enum Role {
    ADMIN,      // Full system access
    SELLER,     // Can manage own products & orders
    USER        // Can browse & purchase
}
```

**Role Hierarchy:**
```
ADMIN
  └─ SELLER (ADMIN can perform all SELLER actions)
       └─ USER (SELLER can perform all USER actions)
```

**Endpoint Protection:**
```java
@RestController
public class ProductController {
    
    // Public access
    @GetMapping("/api/v1/products")
    public ApiResponse<List<ProductResponseDto>> getAll() { }
    
    // Authenticated users only
    @PostMapping("/api/v1/products/{id}/reviews")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ReviewResponseDto> addReview(...) { }
    
    // Admin or Seller only
    @PostMapping("/api/v1/products")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ApiResponse<ProductResponseDto> create(...) { }
    
    // Admin only
    @DeleteMapping("/api/v1/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(...) { }
    
    // Owner or Admin
    @PutMapping("/api/v1/products/{id}")
    @PreAuthorize("isAuthenticated() and (@securityUtils.isProductOwner(#id) or hasRole('ADMIN'))")
    public ApiResponse<ProductResponseDto> update(...) { }
}
```

---

## 🔑 Password Security

### Password Requirements

```java
public class PasswordValidator {
    private static final String PASSWORD_REGEX = 
        "^(?=.*[a-z])"           // At least one lowercase
        + "(?=.*[A-Z])"           // At least one uppercase
        + "(?=.*\\d)"             // At least one digit
        + "(?=.*[@$!%*?&])"       // At least one special char
        + "[a-zA-Z\\d@$!%*?&]{8,}$";  // 8+ characters
}
```

**Requirements:**
- ✅ Minimum 8 characters
- ✅ At least 1 uppercase letter (A-Z)
- ✅ At least 1 lowercase letter (a-z)
- ✅ At least 1 digit (0-9)
- ✅ At least 1 special character (@$!%*?&)

### Password Hashing

```java
@Service
public class PasswordService {
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
    
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
```

**Algorithm**: BCrypt with configurable strength (default: 10)
- Cost factor: 10 (iterates 2^10 times)
- Salt: Generated per password
- Time: ~1 second per hash (prevents brute force)

### Password Reset Security

```java
@Service
@RequiredArgsConstructor
public class PasswordResetService {
    
    @Transactional
    public void initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        // Generate unique token
        String resetToken = generateSecureToken();
        
        // Store token with 24-hour expiration
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(resetToken);
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusHours(24));
        passwordResetTokenRepository.save(token);
        
        // Send email with reset link
        String resetLink = "https://yourdomain.com/reset-password?token=" + resetToken;
        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
    }
    
    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        random.nextBytes(tokenBytes);
        return Base64.getEncoder().encodeToString(tokenBytes);
    }
}
```

**Security Features:**
- ✅ Token generated using SecureRandom (cryptographically strong)
- ✅ Token expires after 24 hours
- ✅ Token can only be used once
- ✅ New password must pass validation
- ✅ Old password NOT required (prevents account lockout)

---

## 🛡️ API Security

### CSRF Protection

**Enabled by default in Spring Security:**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf()
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .and()
            // ...
        return http.build();
    }
}
```

**CSRF Token Requirements:**
- ✅ Token included in `X-CSRF-TOKEN` header or `_csrf` form parameter
- ✅ Token validated for POST, PUT, DELETE, PATCH requests
- ✅ GET, HEAD, TRACE, OPTIONS exempt from CSRF
- ✅ Token renewed on each login

### CORS Configuration

**Allowed Origins (Development):**
```yaml
spring:
  web:
    cors:
      allowed-origins:
        - http://localhost:3000
        - http://localhost:8080
      allowed-methods: GET,POST,PUT,DELETE,OPTIONS
      allowed-headers: "*"
      allow-credentials: true
      max-age: 3600
```

**Production CORS:**
```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("https://yourdomain.com")
                    .allowedMethods("GET", "POST", "PUT", "DELETE")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600);
            }
        };
    }
}
```

### Security Headers

```java
@Configuration
public class SecurityHeadersConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .headers()
                .contentSecurityPolicy("default-src 'self'")
                .and()
                .xssProtection()
                .and()
                .frameOptions().deny()
                .and()
                .httpStrictTransportSecurity()
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains()
                .and();
        return http.build();
    }
}
```

**Headers Added:**
- ✅ `X-Content-Type-Options: nosniff` - Prevents MIME-sniffing
- ✅ `X-Frame-Options: DENY` - Prevents clickjacking
- ✅ `X-XSS-Protection: 1; mode=block` - Enables XSS filter
- ✅ `Content-Security-Policy` - Restricts resource loading
- ✅ `Strict-Transport-Security` - Forces HTTPS

---

## 🔒 Data Protection

### Sensitive Data Masking

```java
@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
private String password;  // Never serialized

@JsonIgnore
private String ssn;  // Completely hidden

// Mask in responses
public SellerApplicationResponse toResponse(SellerApplication entity) {
    response.setAccountNumber(maskAccountNumber(entity.getAccountNumber()));
    response.setPanNumber(maskPanNumber(entity.getPanNumber()));
    return response;
}

private String maskAccountNumber(String accountNumber) {
    if (accountNumber == null || accountNumber.length() < 4) {
        return accountNumber;
    }
    int length = accountNumber.length();
    return "*".repeat(length - 4) + accountNumber.substring(length - 4);
}

private String maskPanNumber(String panNumber) {
    // Example: AAAPA1234B123C → AAAPA****B123C
    if (panNumber == null || panNumber.length() < 8) {
        return panNumber;
    }
    return panNumber.substring(0, 5) + "****" + panNumber.substring(9);
}
```

### Secure Logging

```java
@Service
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    @Transactional
    public Order createOrder(OrderRequest request, User user) {
        // ✅ Log action (safe)
        logger.info("Creating order for user: {}", user.getId());
        
        // ❌ DON'T LOG (unsafe - exposes payment data)
        // logger.info("Creating order: {}", request);
        
        // ✅ DO LOG (safe - only IDs)
        logger.info("Order items: {}", 
            request.getItems().stream()
                .map(item -> "ProductID: " + item.getProductId())
                .collect(Collectors.toList()));
        
        Order order = new Order(request, user);
        return orderRepository.save(order);
    }
}
```

### SQL Injection Prevention

**Safe (Using Spring Data JPA):**
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);
}
```

**Safe (Using parameterized queries):**
```java
// Parameterized query - prevents SQL injection
String query = "SELECT * FROM users WHERE email = ? AND status = ?";
List<User> users = jdbcTemplate.query(query, 
    new Object[]{email, status}, 
    new UserRowMapper());
```

**Unsafe (Using string concatenation - NEVER DO THIS):**
```java
// ❌ VULNERABLE TO SQL INJECTION
String query = "SELECT * FROM users WHERE email = '" + email + "'";
```

---

## 🚨 Rate Limiting & Brute Force Protection

### Authentication Rate Limiting

```java
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final PasswordService passwordService;
    private final LoginAttemptService loginAttemptService;
    
    public AuthResponse login(LoginRequest request) {
        // Check if account is locked
        if (loginAttemptService.isAccountLocked(request.getEmail())) {
            throw new AccountLockedException("Account locked due to failed login attempts");
        }
        
        User user = userService.getUserByEmail(request.getEmail());
        
        if (!passwordService.verifyPassword(request.getPassword(), user.getPasswordHash())) {
            // Increment failed attempt counter
            loginAttemptService.recordFailedLogin(request.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }
        
        // Reset failed attempts on success
        loginAttemptService.resetFailedLogins(request.getEmail());
        
        return generateTokens(user);
    }
}

@Service
public class LoginAttemptService {
    private final LoadingCache<String, Integer> attemptsCache;
    private final int MAX_ATTEMPTS = 5;
    private final int LOCK_DURATION_MINUTES = 30;
    
    public LoginAttemptService() {
        this.attemptsCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(LOCK_DURATION_MINUTES, TimeUnit.MINUTES)
            .build(new CacheLoader<String, Integer>() {
                @Override
                public Integer load(String key) {
                    return 0;
                }
            });
    }
    
    public void recordFailedLogin(String email) {
        int attempts = attemptsCache.getUnchecked(email);
        attemptsCache.put(email, attempts + 1);
    }
    
    public boolean isAccountLocked(String email) {
        return attemptsCache.getUnchecked(email) >= MAX_ATTEMPTS;
    }
}
```

---

## 🔄 Secure Token Refresh

```java
@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {
    
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(generateSecureToken());
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        return refreshTokenRepository.save(refreshToken);
    }
    
    public String refreshAccessToken(String refreshTokenString) {
        RefreshToken refreshToken = refreshTokenRepository
            .findByToken(refreshTokenString)
            .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));
        
        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenExpiredException("Refresh token expired");
        }
        
        User user = refreshToken.getUser();
        String newAccessToken = jwtUtil.generateAccessToken(user);
        
        return newAccessToken;
    }
    
    public void revokeRefreshToken(String refreshTokenString) {
        refreshTokenRepository.deleteByToken(refreshTokenString);
    }
    
    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        random.nextBytes(tokenBytes);
        return Base64.getEncoder().encodeToString(tokenBytes);
    }
}
```

---

## 🔍 Audit Logging

```java
@Entity
@Data
@CreationTimestamp
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    private String action;  // CREATE, UPDATE, DELETE, LOGIN, etc.
    private String entityType;  // User, Product, Order, etc.
    private Long entityId;
    private String changes;  // JSON of what changed
    private LocalDateTime timestamp;
    private String ipAddress;
    private String userAgent;
}

@Aspect
@Component
@RequiredArgsConstructor
public class AuditLoggingAspect {
    
    @After("@annotation(com.abhishek.ecommerce.common.annotation.Audited)")
    public void logAuditEvent(JoinPoint joinPoint) {
        String action = joinPoint.getSignature().getName();
        String entityType = joinPoint.getTarget().getClass().getSimpleName();
        
        auditLogRepository.save(new AuditLog(
            SecurityUtils.getCurrentUserId(),
            action,
            entityType,
            extractEntityId(joinPoint),
            extractChanges(joinPoint),
            LocalDateTime.now(),
            ServletUriComponentsBuilder.fromCurrentRequest()
                .getIpAddress()
        ));
    }
}
```

---

## 🚀 Deployment Security

### Environment Variables (Never Hardcoded)

```yaml
# ✅ CORRECT - Using environment variables
spring:
  datasource:
    password: ${DB_PASSWORD}
  
  security:
    oauth2:
      client:
        registration:
          google:
            client-secret: ${GOOGLE_CLIENT_SECRET}

config:
  jwt:
    access-secret: ${JWT_ACCESS_SECRET}
    refresh-secret: ${JWT_REFRESH_SECRET}
```

```yaml
# ❌ WRONG - Hardcoded secrets
spring:
  datasource:
    password: MySecretPassword123!
```

### Secrets Management (Production)

**Option 1: Render Environment Variables**
```bash
# Set in Render Dashboard
DATABASE_URL=postgresql://...
JWT_ACCESS_SECRET=<strong-32-char-secret>
GOOGLE_CLIENT_SECRET=<secret>
```

**Option 2: AWS Secrets Manager**
```java
@Configuration
public class SecretsConfig {
    @Bean
    public SecretsManagerClient secretsClient() {
        return SecretsManagerClient.builder().build();
    }
    
    @Bean
    public String dbPassword(SecretsManagerClient client) {
        return client.getSecretValue(r -> r.secretId("db-password"))
            .secretString();
    }
}
```

**Option 3: HashiCorp Vault**
```yaml
spring:
  cloud:
    vault:
      host: vault.example.com
      port: 8200
      scheme: https
      token: ${VAULT_TOKEN}
      generic:
        enabled: true
        backend-path: secret
```

---

## ✅ Security Checklist

**Before Production:**
- [ ] All environment variables configured (including ADMIN_EMAIL, ADMIN_PASSWORD, ADMIN_FULL_NAME)
- [ ] JWT secrets are strong (32+ characters)
- [ ] Admin password is strong (16+ chars, mixed case, numbers, symbols)
- [ ] HTTPS enforced (redirect from HTTP)
- [ ] Database credentials secured
- [ ] CORS configured for trusted origins only
- [ ] Rate limiting enabled on login
- [ ] Password requirements enforced
- [ ] Audit logging enabled
- [ ] Sensitive data masked in logs (admin credentials NOT logged in plaintext)
- [ ] SQL injection prevention verified
- [ ] CSRF protection enabled
- [ ] Security headers configured
- [ ] OAuth2 secrets never logged
- [ ] Refresh tokens stored securely
- [ ] OAuth2 redirect URI uses HTTPS
- [ ] Admin bootstrap only runs on dev/prod (not test)
- [ ] Admin credentials rotated periodically
- [ ] Regular security updates applied

---

## 👨‍💼 Admin Account Security

### Bootstrap Configuration

Admin accounts are bootstrapped on application startup using environment variables:

```yaml
config:
  admin:
    email: ${ADMIN_EMAIL}              # From environment
    password: ${ADMIN_PASSWORD}        # From environment  
    full-name: ${ADMIN_FULL_NAME}      # From environment
```

**Security Properties:**
- ✅ **No Hardcoding**: Credentials from environment variables only
- ✅ **Password Encoding**: BCrypt with salt (1 second hash time)
- ✅ **Validation**: Email format validated, fields required
- ✅ **Idempotent**: Won't create duplicate admins
- ✅ **Profile-Aware**: Different credentials per environment
- ✅ **Graceful**: Bootstrap failure doesn't prevent app startup
- ✅ **Audit Logging**: Creation logged for security trail

### Strong Admin Password Requirements

```
✅ Good: ProductionAdmin@2024!SecureHash
✅ Good: Secure-P@ssw0rd-Admin-2024
❌ Bad: admin123 (too simple)
❌ Bad: password (dictionary word)
❌ Bad: Admin@2024 (too short - min 16 chars recommended)
```

**Requirements:**
- Minimum 16 characters
- Mix of upper and lowercase letters
- Include numbers
- Include special characters (@, !, #, $, %, ^, &, *)
- Not a dictionary word
- Not common patterns (111111, qwerty, abcdef, etc.)

### Environment Variable Examples

**Development:**
```bash
export ADMIN_EMAIL="admin@dev.company.com"
export ADMIN_PASSWORD="DevAdminPass@2024"
export ADMIN_FULL_NAME="Development Administrator"
```

**Production:**
```bash
export ADMIN_EMAIL="admin@production.company.com"
export ADMIN_PASSWORD="SecureProductionAdmin@Pass2024!"
export ADMIN_FULL_NAME="Production Administrator"
```

**Never:**
```bash
❌ Commit to git
❌ Log to files
❌ Expose in URLs
❌ Share in email
❌ Store in version control
```

### Admin Password Change Best Practices

1. **Initial Login** - Use bootstrap credentials
2. **Change Password** - Update in admin profile immediately
3. **Secure Storage** - Use password manager for new password
4. **Rotation Policy** - Change every 30-90 days
5. **Audit Trail** - Monitor admin login attempts

---

## 🔗 Security Resources

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [JWT.io Security Best Practices](https://tools.ietf.org/html/rfc7519)
- [CWE Top 25](https://cwe.mitre.org/top25/)
- [NIST Password Guidelines](https://pages.nist.gov/800-63-3/sp800-63b.html)

---

**Last Updated:** 2024-01-25
**Review Frequency:** Quarterly
