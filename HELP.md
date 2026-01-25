# HELP - FAQ & Troubleshooting Guide

## 🆘 Common Issues & Solutions

### Database & Connection Issues

#### ❌ "Connection refused to MySQL"
**Problem:** `java.net.ConnectException: Connection refused`

**Solutions:**
1. Check MySQL is running:
```bash
# Windows
sc query MySQL80

# Linux
sudo systemctl status mysql

# macOS
brew services list | grep mysql
```

2. Verify connection parameters in `.env`:
```dotenv
DB_HOST=localhost
DB_PORT=3306
DB_NAME=ecommerce_backend_db
DB_USER=ecom_user
DB_PASSWORD=correct_password
```

3. Test MySQL connection:
```bash
mysql -h localhost -u ecom_user -p ecommerce_backend_db
```

4. Check firewall rules (port 3306 must be accessible)

---

#### ❌ "Could not resolve placeholder 'DB_PASSWORD'"
**Problem:** Missing environment variable

**Solutions:**
1. Check `.env` file exists:
```bash
ls -la .env
```

2. Verify variable is set:
```powershell
# Windows
Get-Item env:DB_PASSWORD

# Linux/Mac
echo $DB_PASSWORD
```

3. Set environment variable:
```powershell
# Windows
setx DB_PASSWORD "your_password"

# Linux/Mac
export DB_PASSWORD="your_password"
```

4. Restart IDE/terminal for changes to take effect

---

#### ❌ "Access denied for user 'ecom_user'@'localhost'"
**Problem:** Wrong username or password

**Solutions:**
1. Check MySQL user exists:
```sql
SELECT User, Host FROM mysql.user WHERE User='ecom_user';
```

2. Reset password:
```sql
ALTER USER 'ecom_user'@'localhost' IDENTIFIED BY 'new_password';
FLUSH PRIVILEGES;
```

3. Grant permissions:
```sql
GRANT ALL PRIVILEGES ON ecommerce_backend_db.* TO 'ecom_user'@'localhost';
FLUSH PRIVILEGES;
```

---

### Authentication Issues

#### ❌ "401 Unauthorized - Invalid JWT"
**Problem:** JWT token expired or malformed

**Solutions:**
1. Check token format (must start with "Bearer "):
```bash
curl -H "Authorization: Bearer eyJhbGc..." http://localhost:8080/api/v1/users/me
```

2. Get fresh token via refresh:
```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"your_refresh_token"}'
```

3. Check token expiration:
```bash
# Decode JWT (use jwt.io)
eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

---

#### ❌ "403 Forbidden - Access Denied"
**Problem:** User lacks required role/permissions

**Solutions:**
1. Check user role:
```bash
curl -H "Authorization: Bearer {token}" \
  http://localhost:8080/api/v1/users/me
```

2. Verify endpoint permissions in `SecurityConfig.java`:
```java
.antMatchers("/admin/**").hasRole("ADMIN")
.antMatchers("/api/v1/products/create").hasAnyRole("ADMIN", "SELLER")
```

3. Grant role to user (Admin only):
```bash
curl -X POST http://localhost:8080/api/v1/admin/users/{userId}/roles \
  -H "Authorization: Bearer {admin_token}" \
  -H "Content-Type: application/json" \
  -d '{"role":"SELLER"}'
```

---

### Build & Compilation Issues

#### ❌ "Java version not supported"
**Problem:** Project requires Java 21, but Java 8/11 installed

**Solutions:**
1. Check Java version:
```bash
java -version
```

2. Install Java 21:
- **Windows**: https://www.oracle.com/java/technologies/downloads/#java21
- **Linux**: `sudo apt install openjdk-21-jdk`
- **macOS**: `brew install openjdk@21`

3. Set JAVA_HOME:
```powershell
# Windows
setx JAVA_HOME "C:\Program Files\Java\jdk-21"

# Linux/Mac
export JAVA_HOME=/usr/libexec/java_home -v 21
```

---

#### ❌ "Maven build failure - Compilation error"
**Problem:** Syntax or dependency errors

**Solutions:**
1. Clean rebuild:
```bash
mvn clean compile
```

2. Update dependencies:
```bash
mvn clean install -U
```

3. Check for syntax errors in code
4. Verify all imports are correct

---

### Application Startup Issues

#### ❌ "Port 8080 already in use"
**Problem:** Another application using port 8080

**Solutions:**
1. Find process using port:
```bash
# Windows
netstat -ano | findstr :8080

# Linux
lsof -i :8080

# macOS
lsof -i :8080
```

2. Kill process:
```bash
# Windows
taskkill /PID {process_id} /F

# Linux/Mac
kill -9 {process_id}
```

3. Use different port:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

---

#### ❌ "Failed to create bean 'xxx'"
**Problem:** Spring dependency injection or configuration error

**Solutions:**
1. Check error message for missing bean:
```
No qualifying bean of type 'com.example.UserService' available
```

2. Verify bean is annotated:
```java
@Service
public class UserService { ... }
```

3. Check for circular dependencies:
```
BeanA → BeanB → BeanC → BeanA
```

4. Verify component scan configuration

---

### Test Failures

#### ❌ "Test failed - H2 database initialization error"
**Problem:** Test profile configuration issue

**Solutions:**
1. Verify test profile exists:
```bash
ls -la src/main/resources/application-test.yml
```

2. Check test environment variables:
```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
```

3. Run specific test with verbose output:
```bash
mvn test -Dtest=UserServiceTest -X
```

---

#### ❌ "Tests pass locally but fail in CI/CD"
**Problem:** Environment differences between local and CI

**Solutions:**
1. Set test environment variables in CI config
2. Use same Java version as local
3. Check database initialization in CI
4. Run full test suite locally before pushing

---

### API Issues

#### ❌ "400 Bad Request - Invalid JSON"
**Problem:** Request body malformed

**Solutions:**
1. Check Content-Type header:
```bash
curl -H "Content-Type: application/json" ...
```

2. Validate JSON:
```bash
# Use online JSON validator or
python -m json.tool < request.json
```

3. Check required fields in DTO

---

#### ❌ "404 Not Found - Endpoint not found"
**Problem:** Wrong endpoint path

**Solutions:**
1. Check endpoint mapping:
```java
@GetMapping("/api/v1/products/{id}")
public ApiResponse<ProductResponseDto> getProduct(@PathVariable Long id) { ... }
```

2. Verify correct HTTP method (GET, POST, PUT, DELETE)

3. Check Swagger docs:
```
http://localhost:8080/swagger-ui.html
```

---

#### ❌ "500 Internal Server Error"
**Problem:** Unhandled exception in backend

**Solutions:**
1. Check application logs:
```bash
tail -f logs/ecommerce.log
```

2. Check console output for stack trace

3. Enable debug logging:
```yaml
logging:
  level:
    com.abhishek: DEBUG
```

4. Add @ExceptionHandler for custom error handling

---

### Performance Issues

#### ❌ "Slow API response"
**Problem:** Long response times

**Solutions:**
1. Check database query performance:
```sql
EXPLAIN SELECT * FROM products WHERE category_id = 1;
```

2. Add database indexes:
```sql
CREATE INDEX idx_category_id ON products(category_id);
```

3. Enable query caching:
```yaml
spring:
  jpa:
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
```

4. Use pagination:
```bash
GET /api/v1/products?page=0&size=20
```

---

## ❓ Frequently Asked Questions

### Q: How do I reset my password?
**A:**
1. Go to login page: `/auth/login`
2. Click "Forgot Password?"
3. Enter email address
4. Click link in email
5. Set new password

### Q: How do I register as a seller?
**A:**
1. Register as regular user
2. Go to seller dashboard: `/seller/apply`
3. Fill out seller application form
4. Submit documents (PAN, GST, etc.)
5. Wait for admin approval

### Q: How do I reset my JWT secret?
**A:**
1. Generate new secret:
```powershell
[Convert]::ToBase64String([System.Security.Cryptography.RNGCryptoServiceProvider]::new().GetBytes(32))
```

2. Update `.env` file
3. Restart application
4. Existing tokens will be invalid (users need to re-login)

### Q: How do I enable CORS for frontend?
**A:**
Edit `SecurityConfig.java`:
```java
@Bean
public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000", "https://yourdomain.com")
                .allowedMethods("*")
                .allowedHeaders("*");
        }
    };
}
```

### Q: How do I add a new API endpoint?
**A:**
1. Create controller class:
```java
@RestController
@RequestMapping("/api/v1/items")
public class ItemController {
    @GetMapping
    public ApiResponse<List<ItemResponseDto>> getAll() { ... }
}
```

2. Add service and repository
3. Write tests
4. Document in Swagger

### Q: How do I backup database?
**A:**
```bash
# MySQL dump
mysqldump -u ecom_user -p ecommerce_backend_db > backup.sql

# Restore
mysql -u ecom_user -p ecommerce_backend_db < backup.sql
```

### Q: How do I deploy to production?
**A:**
See [DEPLOYMENT.md](DEPLOYMENT.md) for detailed instructions.

### Q: How do I debug an issue?
**A:**
1. Enable debug logging:
```yaml
logging:
  level:
    com.abhishek: DEBUG
```

2. Set breakpoints in IDE
3. Use debugger to inspect variables
4. Check application logs for errors

### Q: How do I handle large file uploads?
**A:**
1. Configure max file size:
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB
```

2. Use async processing for large files
3. Store in cloud (Cloudinary, AWS S3)

### Q: Can I use PostgreSQL instead of MySQL?
**A:**
Yes, update `pom.xml`:
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.6.0</version>
</dependency>
```

Update `application-prod.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ecommerce
    driver-class-name: org.postgresql.Driver
```

### Q: How do I enable rate limiting?
**A:**
Use Spring Cloud Rate Limiter or Bucket4j:
```xml
<dependency>
    <groupId>com.github.vladimir-bukhtoyarov</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>7.6.0</version>
</dependency>
```

---

## 🔗 Useful Resources

- **Spring Boot Docs**: https://spring.io/projects/spring-boot
- **Spring Security**: https://spring.io/projects/spring-security
- **JWT.io**: https://jwt.io/
- **Hibernate Docs**: https://hibernate.org/
- **MySQL Docs**: https://dev.mysql.com/doc/
- **Swagger Editor**: https://editor.swagger.io/
- **Postman**: https://www.postman.com/
- **GitHub Issues**: https://github.com/issues

---

## 📊 Health Check

Verify application is working:

```bash
# All health checks
curl http://localhost:8080/actuator/health

# Specific components
curl http://localhost:8080/actuator/health/db
curl http://localhost:8080/actuator/health/ping

# Application info
curl http://localhost:8080/actuator/info
```

Expected response:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

---

## 📝 Logging & Debugging

### Log Levels
```yaml
logging:
  level:
    root: INFO
    com.abhishek: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
```

### View Logs
```bash
# Real-time logs
tail -f logs/ecommerce.log

# Search for errors
grep ERROR logs/ecommerce.log

# Last 50 lines
tail -50 logs/ecommerce.log
```

### Enable SQL Logging
```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
```

---

## 👨‍💼 Admin Configuration & Bootstrap

### ❓ How do I set admin credentials?

**Admin Configuration Flow:**
```
Environment Variables (ADMIN_EMAIL, ADMIN_PASSWORD, ADMIN_FULL_NAME)
    ↓
YAML Configuration (application-{profile}.yml)
    ↓
AdminProperties Class (config.admin.*)
    ↓
AdminBootstrap Component (creates admin on startup)
    ↓
Database (users table with ROLE_ADMIN)
```

### ✅ Setting Admin Credentials

**1. Set Environment Variables:**

```bash
# Linux/macOS
export ADMIN_EMAIL="admin@example.com"
export ADMIN_PASSWORD="SecurePassword123!"
export ADMIN_FULL_NAME="System Administrator"

# Windows PowerShell
$env:ADMIN_EMAIL="admin@example.com"
$env:ADMIN_PASSWORD="SecurePassword123!"
$env:ADMIN_FULL_NAME="System Administrator"

# Windows Command Prompt
set ADMIN_EMAIL=admin@example.com
set ADMIN_PASSWORD=SecurePassword123!
set ADMIN_FULL_NAME=System Administrator
```

**2. Run Application:**

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

**3. Check Logs for Success:**

```
[WARN] ADMIN USER BOOTSTRAPPED -> email: admin@example.com, fullName: System Administrator
[INFO] Admin user created successfully during application startup
```

**4. Login:**
- Navigate to http://localhost:8080
- Use credentials: admin@example.com / SecurePassword123!

### Profile-Specific Admin Defaults

| Profile | Env Prefix | Default Email | Default Pass |
|---------|-----------|---|---|
| dev | `ADMIN_` | (required) | (required) |
| prod | `ADMIN_` | (required) | (required) |
| h2 | `H2_ADMIN_` | admin@local.dev | AdminPassword123! |
| test | `TEST_ADMIN_` | test-admin@ecommerce.com | TestAdminPassword123! |

### ❌ Admin Not Created?

**Check 1:** Verify environment variables are set
```bash
echo $ADMIN_EMAIL  # Linux/Mac
echo %ADMIN_EMAIL%  # Windows CMD
Get-Item env:ADMIN_EMAIL  # Windows PowerShell
```

**Check 2:** Verify AdminProperties has all three fields
- `ADMIN_EMAIL` - Must be valid email format
- `ADMIN_PASSWORD` - Cannot be empty
- `ADMIN_FULL_NAME` - Cannot be empty

**Check 3:** Check if admin already exists
```sql
SELECT * FROM users WHERE email = 'admin@example.com' AND roles LIKE '%ROLE_ADMIN%';
```
If exists, delete and restart: `DELETE FROM users WHERE email = 'admin@example.com';`

**Check 4:** Review application logs for validation errors
```bash
grep -i "admin\|bootstrap" logs/application.log
```

### ❌ Email Validation Error

**Problem:** `Config validation error: must be a valid email address`

**Solution:** Use valid email format
```
✅ admin@example.com
✅ admin@company.co.uk
❌ adminexample.com (missing @)
❌ admin@ (missing domain)
```

### ❌ Admin Already Exists

**Problem:** App startup log says: `Admin already exists with email: admin@example.com, skipping bootstrap`

**Solutions:**
1. Use different email address:
```bash
export ADMIN_EMAIL="admin2@example.com"
```

2. Or delete and recreate:
```sql
DELETE FROM users WHERE email = 'admin@example.com';
```
Then restart application.

---

## 🎯 Next Steps

1. Check [README.md](README.md) for overview
2. Review [ARCHITECTURE.md](ARCHITECTURE.md) for system design
3. Read [SECURITY.md](SECURITY.md) for security best practices
4. See [DEPLOYMENT.md](DEPLOYMENT.md) for deployment procedures
5. Explore [FEATURES.md](FEATURES.md) for future enhancements

---

**Still need help?** Open an issue on GitHub or contact the development team.
