# Copilot Instructions for Ecommerce Backend

## Architecture Overview

This is a **modular monolithic** Spring Boot e-commerce application (Java 21, Spring Boot 3.2.5). Eight independent business modules (`user`, `product`, `cart`, `order`, `inventory`, `payment`, `notification`, `auth`) share a single database and communicate via method calls. The `common` module provides shared utilities and base entities; `ui` handles Thymeleaf templates and page controllers.

**Key principle**: Modules are cohesive and independently testable, but NOT separate deployments. When modifying cross-module interactions, ensure both modules' tests pass.

## Module Structure Pattern

Every module follows this structure:
```
moduleName/
  ├── service/          # Business logic (interfaces + impl/)
  ├── repository/       # Spring Data JPA repositories
  ├── entity/          # JPA entities with @Entity
  ├── controller/      # REST endpoints or UI controllers
  ├── dto/request/     # Request DTOs with validation
  ├── dto/response/    # Response DTOs
  ├── mapper/          # Manual DTO ↔ Entity mapping (@Component)
  └── exception/       # Custom RuntimeExceptions
```

**Example**: [user](src/main/java/com/abhishek/ecommerce/user) module structure defines all patterns.

## Critical Patterns

### 1. **DTO + Mapper Pattern (Required)**
- **Never** serialize entities directly; always map to response DTOs
- Use `@Component` mappers with explicit methods for CREATE/UPDATE/READ paths
- See [ProductMapper](src/main/java/com/abhishek/ecommerce/product/mapper/ProductMapper.java#L14-L25) for example
- Validate `null` checks in mappers before accessing nested objects (e.g., `product.getCategory() != null ? category.getId()`)

### 2. **Custom Exceptions**
- Create module-specific exceptions extending `RuntimeException` (e.g., `UserNotFoundException`, `ProductAlreadyExistsException`)
- Exceptions are caught and handled by global exception handler in `common/exception/`
- Include meaningful messages: `new ProductNotFoundException("Product ID: " + id + " not found")`

### 3. **Shared Enums & Value Objects**
- Common enums live in [shared/enums](src/main/java/com/abhishek/ecommerce/shared/enums/) (`Role`, `UserStatus`, `ProductStatus`, `AuthProvider`)
- Domain value objects like `Money` in [common/baseEntity](src/main/java/com/abhishek/ecommerce/common/baseEntity/) for reuse across modules

### 4. **Validation**
- Use `@Valid` on controller parameters and `@NotNull`, `@NotBlank` on DTOs
- Validation errors auto-converted to HTTP 400 by Spring's exception handler

### 5. **Async Execution**
- Configured in [AsyncConfig](src/main/java/com/abhishek/ecommerce/config/asyncConfig/AsyncConfig.java)
- Email notifications use `@Async` in [NotificationService](src/main/java/com/abhishek/ecommerce/notification/NotificationService.java)

## Build & Test Commands

```bash
# Build
mvn clean compile

# Run tests (uses H2 profile from application-test.yml)
mvn test

# Run specific test class
mvn test -Dtest=UserServiceTest

# Run application (uses dev profile by default)
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Build JAR
mvn clean package
```

**Profiles**: `dev` (local), `test` (H2 in-memory, fast tests), `prod` (MySQL, Redis).

## Inter-Module Communication

- **Method injection**: Services in one module inject services from another (e.g., `OrderService` → `InventoryService`)
- **No direct entity exposure**: Always return DTOs from service methods
- **Transactional safety**: Use `@Transactional` on multi-step operations across modules
- Example: `OrderService.createOrder()` → calls `InventoryService.reserveStock()` + `PaymentService.processPayment()`

## Testing Requirements

- **Unit tests** use Mockito; mock all external dependencies (repos, other services)
- **Integration tests** use `@SpringBootTest` with H2 database
- See [UserServiceTest](src/test/java/com/abhishek/ecommerce/user/service/UserServiceTest.java) and [EcommerceIntegrationTest](src/test/java/com/abhishek/ecommerce/integration/EcommerceIntegrationTest.java)
- All new features must include test coverage; run `mvn test` before committing

## Dependency Versions & Key Technologies

- **Spring Boot 3.2.5** (Java 21 baseline)
- **Spring Security** + **JWT** (io.jsonwebtoken 0.11.5) for authentication
- **Spring Data JPA** + **MySQL** (prod) / **H2** (test)
- **Redis** (caching, rate limiting via Bucket4j)
- **Lombok** + **MapStruct** (boilerplate reduction)
- **Thymeleaf** + **Spring Security extras** for templating
- **SpringDoc OpenAPI** for Swagger docs

## Configuration & Environment

- Profile-specific YAML in [src/main/resources](src/main/resources): `application-{dev|test|prod}.yml`
- Logging configured in [logback-spring.xml](src/main/resources/logback-spring.xml)
- Email config: [EmailConfig](src/main/java/com/abhishek/ecommerce/config/emailConfig/EmailConfig.java)
- Rate limiting: [Bucket4j](src/main/java/com/abhishek/ecommerce/config) configured per module

## UI Layer

- **Page Controllers**: [ui/product/controller/ProductPageController](src/main/java/com/abhishek/ecommerce/ui/product/controller/ProductPageController.java), [ui/cart/controller/CartPageController](src/main/java/com/abhishek/ecommerce/ui/cart/controller/CartPageController.java)
- **Templates**: [src/main/resources/templates](src/main/resources/templates) with Thymeleaf fragments (navbar, footer, alerts)
- **Static Assets**: CSS in [static/css](src/main/resources/static/css/), JS in [static/js](src/main/resources/static/js/)
- Render views via `return "product/list"` (maps to `templates/product/list.html`)

## Common Pitfalls to Avoid

1. **Serializing entities in REST responses** → Always use response DTOs
2. **Hard-coded strings for enum values** → Use enum classes from `shared/enums/`
3. **Mixing async and transactional boundaries** → `@Async` methods cannot share `@Transactional` context
4. **Missing null checks in mappers** → Nested objects may be null; always guard with `!= null` checks
5. **Skipping tests for cross-module changes** → Run full test suite; integration tests catch boundary issues
