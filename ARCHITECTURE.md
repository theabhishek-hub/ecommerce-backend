# ARCHITECTURE - System Design & Technical Details

## 📐 System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                       CLIENT LAYER                              │
│  (Browser/Mobile/Desktop) → HTML/REST API Calls                │
└────────────────────────┬────────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────────┐
│                  WEB/API LAYER                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ Controllers (REST @RestController + UI @Controller)     │  │
│  │  • AuthController, ProductController, OrderController  │  │
│  │  • Admin/Seller/User Page Controllers                   │  │
│  └──────────────────────────────────────────────────────────┘  │
│                         ↓                                        │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ Filters & Interceptors                                  │  │
│  │  • JwtAuthenticationFilter                              │  │
│  │  • SecurityContextFilter                                │  │
│  │  • CORS & CSRF Filters                                  │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────────┐
│             SECURITY & AUTHENTICATION LAYER                     │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ Spring Security Configuration                           │  │
│  │  • JWT Token Validation                                 │  │
│  │  • OAuth2 Integration (Google)                          │  │
│  │  • Role-Based Access Control (RBAC)                     │  │
│  │  • Password Encoding (BCrypt)                           │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────────┐
│              BUSINESS LOGIC LAYER                               │
│  ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌────────────┐  │
│  │   User     │ │  Product   │ │   Order    │ │  Payment   │  │
│  │  Service   │ │  Service   │ │  Service   │ │  Service   │  │
│  └────────────┘ └────────────┘ └────────────┘ └────────────┘  │
│  ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌────────────┐  │
│  │   Cart     │ │ Inventory  │ │ Notification│ │   Auth    │  │
│  │  Service   │ │  Service   │ │  Service   │ │  Service   │  │
│  └────────────┘ └────────────┘ └────────────┘ └────────────┘  │
└────────────────────────┬────────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────────┐
│              DATA LAYER (DTO/Mapper)                            │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ DTOs & Mappers                                          │  │
│  │  • UserRequestDto ↔ UserResponseDto                    │  │
│  │  • ProductRequestDto ↔ ProductResponseDto              │  │
│  │  • OrderRequestDto ↔ OrderResponseDto                  │  │
│  │  • Handled by @Component Mappers                        │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────────┐
│           PERSISTENCE LAYER (JPA/Repositories)                  │
│  ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌────────────┐  │
│  │   User     │ │  Product   │ │   Order    │ │ Payment    │  │
│  │Repository  │ │Repository  │ │Repository  │ │Repository  │  │
│  └────────────┘ └────────────┘ └────────────┘ └────────────┘  │
│  • Spring Data JPA • Hibernat Queries                          │
│  • Custom JPQL/Native Queries • Pagination/Sorting             │
└────────────────────────┬────────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────────┐
│           DATABASE LAYER                                        │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ MySQL / PostgreSQL / H2 (Testing)                       │  │
│  │  • 15+ tables with relationships                        │  │
│  │  • Proper indexing & constraints                        │  │
│  │  • Transactional integrity                              │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🧩 Modular Structure

### Module Dependencies

```
Common Module (Shared Utilities)
│
├── User Module
│   ├── Auth Module (JWT, OAuth2)
│   └── Shared Enums (Role, UserStatus)
│
├── Product Module
│   ├── Category & Brand Sub-modules
│   └── Common Base Classes
│
├── Cart Module
│   ├── Product Module
│   └── User Module
│
├── Inventory Module
│   ├── Product Module
│   └── Common Utilities
│
├── Order Module
│   ├── Product Module
│   ├── User Module
│   ├── Inventory Module (Stock Reservation)
│   ├── Payment Module
│   └── Notification Module (Async Email)
│
├── Payment Module (Razorpay)
│   ├── Order Module
│   └── Common Utilities
│
└── Notification Module (Email)
    ├── User Module
    └── Common Config (EmailConfig)
```

### Module Interaction Patterns

**1. Method Injection (Synchronous)**
```java
@Service
@RequiredArgsConstructor
public class OrderService {
    private final ProductService productService;
    private final InventoryService inventoryService;
    
    public Order createOrder(OrderRequest request) {
        // Call other modules' services
        Product product = productService.getProductById(request.getProductId());
        inventoryService.reserveStock(product.getId(), request.getQuantity());
        // ... create order
    }
}
```

**2. Event Publishing (Asynchronous)**
```java
// Publish event
@Service
public class OrderService {
    private final ApplicationEventPublisher eventPublisher;
    
    public void createOrder(OrderRequest request) {
        // ... create order
        eventPublisher.publishEvent(new OrderCreatedEvent(order));
    }
}

// Listen for event
@Component
public class OrderCreatedEventListener {
    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        // Send notification async
        notificationService.sendOrderConfirmation(event.getOrder());
    }
}
```

---

## 🗄️ Database Schema

### Core Tables

```sql
-- Users & Authentication
users (id, email, password_hash, first_name, last_name, status, created_at)
roles (id, name, description)
user_roles (user_id, role_id)
refresh_tokens (id, user_id, token, expires_at)

-- Products
products (id, name, description, price, category_id, brand_id, seller_id, status)
categories (id, name, description)
brands (id, name, description)
product_images (id, product_id, image_url)

-- Orders & Payments
orders (id, user_id, order_number, total_amount, status, created_at)
order_items (id, order_id, product_id, quantity, price)
payments (id, order_id, razorpay_payment_id, amount, status)

-- Shopping Cart
carts (id, user_id, created_at)
cart_items (id, cart_id, product_id, quantity)

-- Inventory
inventory (id, product_id, quantity_available, quantity_reserved)

-- Seller Management
seller_applications (id, user_id, status, pan_number, gst_number)

-- Notifications
notifications (id, user_id, type, message, sent_at)
```

### Entity Relationships

```
User (1) ──→ (N) Orders
User (1) ──→ (N) Cart
User (1) ──→ (N) RefreshToken
User (1) ──→ (1) SellerApplication

Product (N) ←─ (1) Category
Product (N) ←─ (1) Brand
Product (1) ──→ (N) ProductImages
Product (1) ──→ (1) Inventory

Order (1) ──→ (N) OrderItems
OrderItem (N) ←─ (1) Product

Cart (1) ──→ (N) CartItems
CartItem (N) ←─ (1) Product

Payment (1) ←─ (N) Order
```

---

## 🔐 Security Architecture

### Authentication Flow

```
1. User Registers/Logs In
   ├─ POST /api/v1/auth/register
   ├─ POST /api/v1/auth/login
   └─ POST /api/v1/auth/google (OAuth2)

2. Generate Tokens
   ├─ Access Token (JWT, 15 minutes)
   └─ Refresh Token (DB stored, 7 days)

3. Client sends requests
   ├─ Authorization: Bearer {accessToken}
   └─ JwtAuthenticationFilter validates token

4. Refresh Token when expired
   ├─ POST /api/v1/auth/refresh
   └─ Return new access token + refresh token

5. Logout
   ├─ DELETE /api/v1/auth/logout
   └─ Invalidate refresh token
```

### JWT Structure

```
Header
{
  "alg": "HS512",
  "typ": "JWT"
}

Payload
{
  "sub": "user_id",
  "email": "user@example.com",
  "roles": ["ROLE_USER"],
  "iat": 1516239022,
  "exp": 1516242622
}

Signature = HMACSHA512(base64(header) + "." + base64(payload), SECRET_KEY)
```

### OAuth2 Google Integration

```
User clicks "Sign with Google"
      ↓
Browser redirects to Google
      ↓
User authenticates with Google
      ↓
Google redirects back with auth code
      ↓
Backend exchanges code for Google tokens
      ↓
Backend retrieves user profile from Google
      ↓
Check if user exists in DB
      ├─ If exists: Generate JWT tokens
      └─ If not: Create user + Generate JWT tokens
      ↓
Return JWT access & refresh tokens
```

### Role-Based Access Control (RBAC)

```java
@RestController
public class ProductController {
    // Anyone can read
    @GetMapping("/api/v1/products")
    public ApiResponse<List<ProductResponseDto>> getAllProducts() { ... }
    
    // Only ADMIN or SELLER can create
    @PostMapping("/api/v1/products")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ApiResponse<ProductResponseDto> createProduct(...) { ... }
    
    // Only ADMIN can delete
    @DeleteMapping("/api/v1/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteProduct(...) { ... }
}
```

---

## 🔄 Request/Response Flow

### Example: Create Order

```
1. Client Request
   POST /api/v1/orders
   Authorization: Bearer {jwt_token}
   Content-Type: application/json
   {
     "items": [
       {"productId": 1, "quantity": 2}
     ],
     "shippingAddress": "123 Main St"
   }

2. Security Filter
   ├─ Extract token from header
   ├─ Validate JWT signature
   ├─ Check token expiration
   └─ Load user from Security Context

3. Controller (OrderController)
   ├─ Validate request with @Valid
   └─ Call service layer

4. Service Layer (OrderService)
   ├─ Fetch products from ProductService
   ├─ Calculate total price
   ├─ Reserve inventory via InventoryService
   ├─ Create order & save to DB
   ├─ Process payment via PaymentService
   ├─ Publish OrderCreatedEvent
   └─ Return OrderResponseDto

5. Event Listener (OrderCreatedEventListener)
   ├─ Listen for OrderCreatedEvent
   └─ Call NotificationService.sendOrderConfirmation()

6. Notification Service (Async via @Async)
   ├─ Send email to user
   └─ Send email to seller

7. Response to Client
   {
     "status": "SUCCESS",
     "message": "Order created successfully",
     "data": {
       "id": 1,
       "orderNumber": "ORD-2024-001",
       "totalAmount": 9998.00,
       "status": "PENDING",
       "items": [...]
     }
   }
```

---

## 🚀 Performance Optimization

### Caching Strategy

```java
@Service
public class ProductService {
    @Cacheable("products", key = "#id")
    public ProductResponseDto getProductById(Long id) {
        // Executed only once, result cached
        return productRepository.findById(id)
            .map(productMapper::toResponse)
            .orElseThrow(...);
    }
    
    @CacheEvict(value = "products", key = "#id")
    public void updateProduct(Long id, ProductUpdateRequestDto request) {
        // Updates product and evicts cache
    }
}
```

### Query Optimization

```java
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("""
        SELECT p FROM Product p 
        LEFT JOIN FETCH p.category 
        LEFT JOIN FETCH p.brand
        WHERE p.id = :id
    """)
    Optional<Product> findByIdWithDetails(Long id);
    
    // Pagination for large datasets
    Page<Product> findByStatusOrderByCreatedAtDesc(
        ProductStatus status, 
        Pageable pageable
    );
}
```

### Database Indexes

```sql
-- Frequently searched columns
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_product_category ON products(category_id);
CREATE INDEX idx_order_user ON orders(user_id);
CREATE INDEX idx_order_created_at ON orders(created_at);

-- Foreign keys (auto-indexed)
ALTER TABLE products ADD CONSTRAINT fk_product_category 
    FOREIGN KEY (category_id) REFERENCES categories(id);
```

---

## 🧵 Asynchronous Processing

### Email Notifications (Async)

```java
@Service
@EnableAsync  // Enable in config class
public class NotificationService {
    @Async
    public void sendOrderConfirmation(Order order) {
        // Runs in separate thread pool
        emailService.sendEmail(
            order.getUser().getEmail(),
            "Order Confirmation",
            buildOrderConfirmationEmail(order)
        );
    }
    
    @Async
    public void sendSellerNotification(Order order) {
        // Another async task
        emailService.sendEmail(
            order.getSeller().getEmail(),
            "New Order",
            buildSellerNotificationEmail(order)
        );
    }
}
```

### Thread Pool Configuration

```java
@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-task-");
        executor.initialize();
        return executor;
    }
}
```

---

## 🐛 Error Handling

### Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFound(
        UserNotFoundException ex) {
        ApiResponse<Void> response = ApiResponseBuilder
            .error("User not found: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(
        ValidationException ex) {
        ApiResponse<Void> response = ApiResponseBuilder
            .validationError(ex.getErrors());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
        Exception ex) {
        ApiResponse<Void> response = ApiResponseBuilder
            .error("Internal server error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
```

### Custom Exceptions

```java
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}

public class ProductAlreadyExistsException extends RuntimeException {
    public ProductAlreadyExistsException(String message) {
        super(message);
    }
}

public class InsufficientInventoryException extends RuntimeException {
    public InsufficientInventoryException(String message) {
        super(message);
    }
}
```

---

## 📡 API Response Format

### Success Response

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "statusCode": 200,
  "timestamp": "2024-01-25T12:30:45.123Z",
  "data": {
    "id": 1,
    "name": "Product Name",
    ...
  }
}
```

### Error Response

```json
{
  "success": false,
  "message": "Validation failed",
  "statusCode": 400,
  "timestamp": "2024-01-25T12:30:45.123Z",
  "errors": [
    {
      "field": "email",
      "message": "Email format invalid"
    },
    {
      "field": "password",
      "message": "Password too short"
    }
  ]
}
```

### Paginated Response

```json
{
  "success": true,
  "message": "Products fetched successfully",
  "data": [...],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 100,
  "totalPages": 5,
  "isFirst": true,
  "isLast": false,
  "hasNext": true,
  "hasPrevious": false
}
```

---

## 🔄 Transaction Management

```java
@Service
@RequiredArgsConstructor
public class OrderService {
    
    @Transactional  // Begin transaction here
    public Order createOrder(OrderRequest request) {
        try {
            // All operations in one transaction
            Order order = orderRepository.save(new Order(request));
            inventoryService.reserveStock(order);
            paymentService.processPayment(order);
            return order;
            // Commit on success
        } catch (Exception e) {
            // Rollback on failure
            throw new OrderCreationException("Failed to create order", e);
        }
    }
}
```

---

## 🔍 Monitoring & Metrics

### Actuator Endpoints

```
GET /actuator/health              → Application health
GET /actuator/metrics             → All metrics
GET /actuator/metrics/http.requests.count  → HTTP request count
GET /actuator/metrics/process.uptime       → Application uptime
GET /actuator/env                 → Environment variables
GET /actuator/loggers             → Logging configuration
```

### Custom Metrics

```java
@Component
public class OrderMetrics {
    private final MeterRegistry meterRegistry;
    
    public void recordOrderCreation(Order order) {
        meterRegistry.counter("orders.created").increment();
        meterRegistry.gauge("orders.amount", order.getTotalAmount());
    }
}
```

---

## 📚 API Endpoint Summary

| Module | Endpoint | Method | Auth | Purpose |
|--------|----------|--------|------|---------|
| Auth | `/api/v1/auth/register` | POST | ❌ | User registration |
| Auth | `/api/v1/auth/login` | POST | ❌ | User login |
| Auth | `/api/v1/auth/refresh` | POST | ✅ | Refresh JWT token |
| User | `/api/v1/users` | GET | ✅(ADMIN) | List all users |
| User | `/api/v1/users/me` | GET | ✅ | Get current user profile |
| User | `/api/v1/users/{id}` | PUT | ✅ | Update user profile |
| Product | `/api/v1/products` | GET | ❌ | List products |
| Product | `/api/v1/products` | POST | ✅(ADMIN/SELLER) | Create product |
| Product | `/api/v1/products/{id}` | PUT | ✅ | Update product |
| Category | `/api/v1/categories` | GET | ❌ | List categories |
| Cart | `/api/v1/cart/items` | GET | ✅ | Get cart items |
| Cart | `/api/v1/cart/items` | POST | ✅ | Add to cart |
| Order | `/api/v1/orders` | POST | ✅ | Create order |
| Order | `/api/v1/orders` | GET | ✅ | Get user orders |
| Payment | `/api/v1/payments/razorpay/webhook` | POST | ✅ | Payment webhook |

---

## 🎯 Design Patterns Used

| Pattern | Usage | Example |
|---------|-------|---------|
| MVC | Separation of concerns | Controller, Service, Repository |
| DTO | Data transfer isolation | ProductRequestDto, ProductResponseDto |
| Mapper | Entity/DTO conversion | ProductMapper (@Component) |
| Repository | Data access abstraction | ProductRepository extends JpaRepository |
| Service Locator | Dependency management | @RequiredArgsConstructor autowiring |
| Observer | Event handling | @EventListener, ApplicationEventPublisher |
| Singleton | Shared instances | Spring beans (@Service, @Component) |
| Factory | Object creation | EntityFactory patterns |
| Strategy | Different implementations | PaymentStrategy interface |

---

**For more details, see:**
- [README.md](README.md) - Quick start guide
- [SECURITY.md](SECURITY.md) - Security guidelines
- [DEPLOYMENT.md](DEPLOYMENT.md) - Deployment procedures
- [FEATURES.md](FEATURES.md) - Feature roadmap
