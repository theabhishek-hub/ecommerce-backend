# Testing Guide

## Overview
This project follows a strict testing structure with JUnit 5, Mockito, and limited integration tests. Tests are designed to be fast, maintainable, and focused on business logic without loading full Spring contexts where possible.

## Testing Structure

### 1. Unit Tests (Mockito Only)
- **Framework**: JUnit 5 with `@ExtendWith(MockitoExtension.class)`
- **Scope**: Service-layer business logic only
- **Mocking**: Repositories, Redis template, mail sender, async executor
- **No Spring Context**: Pure unit tests
- **Location**: `src/test/java/com/abhishek/ecommerce/*/service/*ServiceTest.java`

### 2. Controller Tests (WebMvc Slice Tests)
- **Framework**: `@WebMvcTest` with MockMvc
- **Scope**: Request/response, validation, HTTP status codes, global exception handling
- **Mocking**: Service layer with `@MockBean`
- **No DB/Repositories**: Isolated controller testing
- **Location**: `src/test/java/com/abhishek/ecommerce/*/controller/*ControllerTest.java`

### 3. Integration Tests (VERY LIMITED)
- **Only 2 allowed**:
  - `SecurityIntegrationTest`: `@SpringBootTest` + `@AutoConfigureMockMvc` for auth/authorization
  - `OrderHappyPathIntegrationTest`: One end-to-end happy path (place order)
- **Database**: H2 in-memory
- **Profile**: `test` profile disables external systems

## Test Profile Configuration
- **File**: `src/main/resources/application-test.yml`
- **Features**:
  - H2 database
  - Cache disabled (`type: none`)
  - Async disabled (`pool.core-size: 1`)
  - Redis auto-configuration excluded
  - OAuth2 disabled

## Running Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=CategoryServiceTest
```

### Run Tests in Module
```bash
mvn test -Dtest="*ServiceTest"
```

### Skip Tests
```bash
mvn clean install -DskipTests
```

## Test Coverage
- Service layer: Full unit test coverage with Mockito
- Controllers: Request/response validation and auth checks
- Integration: Security and one happy-path flow
- External systems: Mocked (Redis, mail, payment gateways)

## Rules Adhered
- No full end-to-end tests
- No mixed test layers
- No Spring context in unit tests
- No external system testing
- No Testcontainers/WireMock/embedded servers
- Only 2 integration tests maximum

## Dependencies
- `spring-boot-starter-test`: JUnit 5, Mockito, AssertJ
- `spring-security-test`: Security test helpers