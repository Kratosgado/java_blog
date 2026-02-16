# Security Configuration Guide

## Overview

The blogging platform implements comprehensive security measures including JWT-based authentication, role-based access control (RBAC), CORS configuration, and CSRF protection strategies optimized for stateless API architecture.

## JWT Authentication

### What is JWT?

JWT (JSON Web Token) is a stateless authentication mechanism that allows secure information transmission. Each token is:

- **Self-contained**: Includes user claims
- **Digitally signed**: Cannot be tampered with
- **Stateless**: No server session storage required

### Token Structure

```
Header.Payload.Signature
```

**Decoded Example**:

```json
// Header
{
  "alg": "HS256",
  "typ": "JWT"
}

// Payload
{
  "sub": "alice@example.com",
  "userId": 1,
  "roles": ["READER", "AUTHOR"],
  "iat": 1710525000,
  "exp": 1710611400
}

// Signature (HMAC-SHA256)
HMACSHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret)
```

### JWT Configuration

**Location**: `JwtUtil.java` and `SecurityConfig.java`

**Key Configuration**:

```yaml
jwt:
  secret: ${JWT_SECRET:your-secret-key-min-256-bits}
  expiration: ${JWT_EXPIRATION:86400000} # 24 hours in milliseconds
  algorithm: HS256
```

**Environment Variables**:

```bash
JWT_SECRET=your-secret-key-must-be-at-least-256-bits-long-for-hs256
JWT_EXPIRATION=86400000  # 24 hours
```

### Token Generation & Validation

**Token Generation** (on login/register):

```java
// Service layer
public AuthResponse login(LoginRequest request) {
    User user = authenticateUser(request);
    String token = jwtUtil.generateToken(user);
    return new AuthResponse(token, user);
}

// JWT Utility
public String generateToken(User user) {
    return Jwts.builder()
        .setSubject(user.getEmail())
        .claim("userId", user.getId())
        .claim("roles", user.getRoles())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(SignatureAlgorithm.HS256, secret)
        .compact();
}
```

**Token Validation** (on each request):

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) {
        // 1. Extract token from Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        // 2. Check token blacklist (O(1) lookup)
        if (tokenBlacklistService.isBlacklisted(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 3. Validate token signature and expiry
        try {
            Claims claims = jwtUtil.validateToken(token);

            // 4. Load user and set authentication
            Long userId = claims.get("userId", Long.class);
            User user = userService.findById(userId);

            // 5. Create authentication token
            Authentication auth = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities());
            SecurityContextHolder.setContext(
                new SecurityContextImpl(auth));
        } catch (JwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        filterChain.doFilter(request, response);
    }
}
```

### Token Expiration Handling

**Client-Side Strategy**:

1. Store token in localStorage or sessionStorage
2. Before each request, check expiration time
3. If expired, redirect to login
4. Refresh token logic (future enhancement)

**Server-Side Strategy**:

1. Validate expiration on every request
2. Reject expired tokens with 401 Unauthorized
3. Client must login again

---

## Role-Based Access Control (RBAC)

### Role Hierarchy

```
READER (Base role)
  ├── Can browse/read posts
  ├── Can create comments
  ├── Can bookmark posts
  └── (Assigned to new users by default)

AUTHOR (Extends READER)
  ├── All READER permissions
  ├── Can create/edit own posts
  ├── Can manage own comments
  └── Can publish drafts

ADMIN (Full access)
  ├── All AUTHOR permissions
  ├── Can manage any user
  ├── Can manage all posts/comments
  └── Can access system admin endpoints
```

### Authorization Configuration

**Security Config**:

```java
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
            // Endpoint-level authorization
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("GET", "/api/v1/posts/**").permitAll()
                .requestMatchers("GET", "/api/v1/tags/**").permitAll()

                // Authenticated endpoints
                .requestMatchers("POST", "/api/v1/posts/**")
                    .hasAnyRole("AUTHOR", "ADMIN")
                .requestMatchers("PUT", "/api/v1/posts/**")
                    .hasAnyRole("AUTHOR", "ADMIN")

                // Admin-only endpoints
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.disable())
            .addFilterBefore(jwtAuthenticationFilter(),
                             UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

### Method-Level Authorization

**Using @SecuredEndpoint Annotation**:

```java
@RestController
public class PostController {
    @PostMapping("/posts")
    @SecuredEndpoint(roles = {UserRole.AUTHOR, UserRole.ADMIN})
    public Post createPost(@RequestBody CreatePostRequest request) {
        // Only AUTHOR or ADMIN can execute
    }

    @PutMapping("/posts/{id}")
    @SecuredEndpoint(roles = {UserRole.AUTHOR, UserRole.ADMIN})
    public Post updatePost(@PathVariable Long id) {
        // Additional check: author can only update own posts
        Long userId = SecurityUtils.getCurrentUserId();
        Post post = postService.getPostById(id);
        if (!post.getAuthorId().equals(userId) &&
            !SecurityUtils.isAdmin()) {
            throw new ForbiddenException("Cannot update other users' posts");
        }
    }
}
```

### Testing RBAC

```bash
# Admin endpoint - success
curl -X GET http://localhost:8080/api/v1/admin/users \
  -H "Authorization: Bearer <admin-token>"
# Response: 200 OK

# Admin endpoint - READER attempts (should fail)
curl -X GET http://localhost:8080/api/v1/admin/users \
  -H "Authorization: Bearer <reader-token>"
# Response: 403 Forbidden

# No token
curl -X GET http://localhost:8080/api/v1/admin/users
# Response: 401 Unauthorized
```

---

## CORS (Cross-Origin Resource Sharing)

### What is CORS?

CORS is a **browser-enforced security mechanism** that controls which web domains can access your API. It implements the Same-Origin Policy (SOP), which by default blocks cross-origin requests to prevent malicious websites from stealing data.

### How CORS Works

```
1. Browser sends PREFLIGHT (OPTIONS) request
   ├─→ Origin: http://localhost:3000
   ├─→ Access-Control-Request-Method: POST
   └─→ Access-Control-Request-Headers: Authorization

2. Server responds with allowed origins/methods
   ├─→ Access-Control-Allow-Origin: http://localhost:3000
   ├─→ Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
   └─→ Access-Control-Allow-Headers: Authorization, Content-Type

3. Browser checks response and either:
   ├─→ Allows actual request (preflight passed)
   └─→ Blocks request (preflight failed) - never sends to server

4. Server executes actual request
```

### CORS Configuration

**Location**: `SecurityConfig.java`

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // Allowed origins (configurable via CORS_ORIGINS environment variable)
    // Default: http://localhost:8080, http://localhost:3000, https://studio.apollographql.com
    String corsOrigins = System.getenv().getOrDefault("CORS_ORIGINS",
        "http://localhost:8080,http://localhost:3000,https://studio.apollographql.com");
    configuration.setAllowedOrigins(Arrays.asList(corsOrigins.split(",")));

    // Allowed HTTP methods
    configuration.setAllowedMethods(
        Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

    // Allowed headers (explicit list for security)
    configuration.setAllowedHeaders(
        Arrays.asList("Authorization", "Content-Type", "Accept",
                     "X-Requested-With", "Cache-Control"));

    // Enable credentials (cookies, Authorization headers)
    configuration.setAllowCredentials(true);

    // Preflight cache duration (1 hour)
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

**Key Points**:

- ✅ **Browser-Enforced**: CORS is enforced by the browser, not the server
- ✅ **Preflight Optimization**: 1-hour cache reduces redundant OPTIONS requests
- ✅ **Specific Headers**: Only necessary headers allowed (never use wildcard `*` with credentials)
- ✅ **Environment-Configurable**: Origins can be customized via `CORS_ORIGINS` env variable
- ❌ **Not Protection Against**: CORS does NOT protect against server-to-server attacks

### Testing CORS

```bash
# Test preflight request (valid origin)
curl -X OPTIONS http://localhost:8080/api/v1/posts \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Authorization" \
  -v

# Expected response headers:
# HTTP/1.1 200 OK
# Access-Control-Allow-Origin: http://localhost:3000
# Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, PATCH
# Access-Control-Allow-Headers: Authorization, Content-Type, Accept, X-Requested-With, Cache-Control
# Access-Control-Allow-Credentials: true
# Access-Control-Max-Age: 3600

# Test with invalid origin (browser would block)
curl -X GET http://localhost:8080/api/v1/posts \
  -H "Origin: http://malicious-site.com" \
  -v
# Server still returns 200, but browser blocks response due to missing CORS header
```

---

## CSRF (Cross-Site Request Forgery)

### What is CSRF?

CSRF is a **server-side security vulnerability** where an attacker tricks a user's browser into making unwanted requests to a website where the user is authenticated.

### Attack Example

```
1. User logs into bank.com (receives session cookie)
2. User visits evil.com (in another tab)
3. evil.com submits hidden form: <form action="bank.com/transfer?amount=1000&to=attacker">
4. Browser automatically sends session cookie
5. bank.com processes transfer (thinks it's legitimate user)
```

### Why CSRF Protection is DISABLED for Our JWT API

| Aspect | Session-Based (CSRF Vulnerable) | JWT-Based (CSRF Immune) |
|--------|-------------------------------------|------------------------------| | **Storage** | Cookies (automatic) | localStorage/sessionStorage (manual) |
| **Transmission** | Automatically sent by browser | Manually added to Authorization header |
| **CSRF Risk** | ⚠️ High | ✅ None |
| **XSS Risk** | ✅ Low (HttpOnly) | ⚠️ Higher (JS can access localStorage) |
| **Protection** | CSRF tokens required | Input sanitization + CORS |

**Configuration**:

```java
http.csrf(csrf -> csrf.disable())
```

**Why Our JWT API is Safe**:

1. ✅ **No Automatic Transmission**: JWTs must be manually added to Authorization header
2. ✅ **Stateless**: No server-side session; tokens are self-contained
3. ✅ **CORS Protection**: Strict CORS policy prevents unauthorized domains
4. ✅ **Token Validation**: Every request validates JWT signature and expiry

### CORS vs CSRF: Key Differences

| Feature                  | CORS                                       | CSRF                                               |
| ------------------------ | ------------------------------------------ | -------------------------------------------------- |
| **What it protects**     | Data confidentiality (reading responses)   | Action integrity (preventing unwanted requests)    |
| **Enforced by**          | Browser (client-side)                      | Server (backend validation)                        |
| **Attack scenario**      | Malicious site tries to read API responses | Malicious site tricks user into submitting request |
| **Vulnerability**        | Same-Origin Policy bypass                  | Automatic cookie submission                        |
| **Protection mechanism** | Allow/deny specific origins                | Validate unique token per session                  |
| **Applies to**           | All cross-origin requests                  | State-changing requests (POST/PUT/DELETE)          |
| **Our implementation**   | ✅ Enabled with strict origin list         | ❌ Disabled (stateless JWT API)                    |

---

## Token Blacklist (For Logout)

### Purpose

Implement JWT token revocation for logout functionality using HashMap-based blacklist with O(1) lookup performance.

### Data Structure

```java
@Service
public class TokenBlacklistService {
    private final ConcurrentHashMap<String, Long> blacklist =
        new ConcurrentHashMap<>();

    // Add token to blacklist (O(1))
    public void blacklistToken(String token, long expiryTimestamp) {
        blacklist.put(token, expiryTimestamp);
    }

    // Check if token is blacklisted (O(1))
    public boolean isBlacklisted(String token) {
        Long expiryTimestamp = blacklist.get(token);
        if (expiryTimestamp == null) return false;

        // Lazy expiry: remove expired tokens on access
        if (System.currentTimeMillis() > expiryTimestamp) {
            blacklist.remove(token);
            return false;
        }
        return true;
    }

    // Scheduled cleanup (runs hourly)
    @Scheduled(fixedRate = 3600000)
    public void cleanupExpiredTokens() {
        long currentTime = System.currentTimeMillis();
        blacklist.entrySet().removeIf(entry ->
            currentTime > entry.getValue());
    }
}
```

### Logout Flow

```
POST /api/v1/auth/logout
  ├─→ Extract JWT from Authorization header
  ├─→ Parse JWT to get expiration time
  ├─→ Add token to blacklist with expiration timestamp
  └─→ Return success response

Subsequent requests with blacklisted token
  ├─→ JWT filter checks blacklist (O(1) lookup)
  ├─→ Token found in blacklist → 401 Unauthorized
  └─→ Request rejected before reaching controllers
```

### Usage Example

```bash
# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"password123"}'
# Response: { "accessToken": "eyJhbGc..." }

# Use token
curl -X GET http://localhost:8080/api/v1/posts \
  -H "Authorization: Bearer eyJhbGc..."
# Response: 200 OK with posts

# Logout - blacklist token
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer eyJhbGc..."
# Response: { "message": "Logout successful", "tokenBlacklisted": true }

# Try to use blacklisted token
curl -X GET http://localhost:8080/api/v1/posts \
  -H "Authorization: Bearer eyJhbGc..."
# Response: 401 Unauthorized { "error": "Token has been revoked" }
```

---

## Brute-Force Attack Prevention

### Purpose

Prevent credential stuffing and brute-force attacks with account lockout after N failed attempts.

### Configuration

```yaml
security:
  login:
    max-attempts: 5 # Lockout after 5 failed attempts
    lockout-duration-ms: 900000 # 15 minutes
```

### Implementation

```java
@Service
public class LoginAttemptService {
    @Value("${security.login.max-attempts:5}")
    private int MAX_ATTEMPTS;

    @Value("${security.login.lockout-duration-ms:900000}")
    private long LOCKOUT_DURATION_MS;  // 15 minutes

    private final ConcurrentHashMap<String, LoginAttemptRecord> attemptCache
        = new ConcurrentHashMap<>();

    // Record failed attempt (O(1))
    public void recordFailedAttempt(String username) {
        String key = username.toLowerCase();
        attemptCache.compute(key, (k, record) ->
            record == null
                ? new LoginAttemptRecord(1, System.currentTimeMillis())
                : new LoginAttemptRecord(record.attemptCount + 1,
                                         record.firstAttemptTimestamp)
        );
    }

    // Check if user is blocked (O(1))
    public boolean isBlocked(String username) {
        LoginAttemptRecord record = attemptCache.get(username.toLowerCase());
        if (record == null) return false;

        long elapsed = System.currentTimeMillis() - record.firstAttemptTimestamp;
        if (elapsed > LOCKOUT_DURATION_MS) {
            attemptCache.remove(username.toLowerCase());
            return false;
        }

        return record.attemptCount >= MAX_ATTEMPTS;
    }

    // Clear attempts on successful login (O(1))
    public void recordSuccessfulAttempt(String username) {
        attemptCache.remove(username.toLowerCase());
    }
}
```

### Login Flow with Protection

```java
public AuthResponse login(LoginRequest request) {
    String email = request.email();

    // Check if account is blocked
    if (loginAttemptService.isBlocked(email)) {
        long remainingTime = getRemainingLockoutTime(email);
        throw new InvalidRequestException(
            String.format("Account locked. Try again in %d seconds",
                          remainingTime / 1000));
    }

    // Authenticate user
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> {
            loginAttemptService.recordFailedAttempt(email);
            return new UnauthorizedException("Invalid credentials");
        });

    // Verify password
    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
        loginAttemptService.recordFailedAttempt(email);
        throw new UnauthorizedException("Invalid credentials");
    }

    // Success - clear failed attempts
    loginAttemptService.recordSuccessfulAttempt(email);
    return authService.createAuthResponse(user);
}
```

### Test Scenarios

```bash
# Failed attempt 1
curl -X POST http://localhost:8080/api/v1/auth/login \
  -d '{"email":"alice@example.com","password":"wrong"}'
# Response: 401 Unauthorized

# Attempts 2-4: Same as above

# Failed attempt 5 - Account locked
curl -X POST http://localhost:8080/api/v1/auth/login \
  -d '{"email":"alice@example.com","password":"wrong"}'
# Response: 400 Bad Request
# { "error": "Account locked. Try again in 897 seconds" }

# Correct password still fails while locked
curl -X POST http://localhost:8080/api/v1/auth/login \
  -d '{"email":"alice@example.com","password":"password123"}'
# Response: 400 Bad Request (still locked)

# Wait 15 minutes or manual unlock...
# Account unlocked
curl -X POST http://localhost:8080/api/v1/auth/login \
  -d '{"email":"alice@example.com","password":"password123"}'
# Response: 200 OK
```

---

## BCrypt Password Hashing

### Configuration

```java
@Bean
public PasswordEncoder passwordEncoder() {
    // Cost factor 12 = 2^12 = 4,096 key expansion rounds
    // ~400ms per hash (protects against brute-force GPU attacks)
    return new BCryptPasswordEncoder(12);
}
```

### Cost Factor Analysis

| Factor          | Iterations | Hash Time | Security Level  |
| --------------- | ---------- | --------- | --------------- |
| 10 (default)    | 1,024      | ~100ms    | Acceptable      |
| 12 (our config) | 4,096      | ~400ms    | **Recommended** |
| 14              | 16,384     | ~1,600ms  | Very Strong     |
| 16              | 65,536     | ~6,400ms  | Overkill        |

### Why Cost Factor 12?

- ✅ **4× more expensive** than default (cost 10)
- ✅ **GPU resistance**: Makes GPU-accelerated attacks harder
- ✅ **Acceptable latency**: ~400ms for login is acceptable
- ✅ **Future-proof**: Provides security margin as hardware improves

### Usage

```java
// During registration/password change
String hashedPassword = passwordEncoder.encode(plainPassword);
user.setPassword(hashedPassword);

// During login
if (passwordEncoder.matches(inputPassword, storedHash)) {
    // Correct password
}
```

---

## Security Event Logging

### Monitored Events

1. **Authentication Success** - Username, roles, IP, timestamp
2. **Authentication Failure** - Username, reason, IP, timestamp
3. **Authorization Denied** - User, endpoint, required role
4. **JWT Validation Failures** - Token validation errors
5. **Account Lockout** - Username, attempt count

### Log Examples

```
[INFO]  ✓ AUTHENTICATION SUCCESS | User: alice@example.com | Roles: [ROLE_READER] | IP: 192.168.1.100

[WARN]  ✗ AUTHENTICATION FAILURE | User: attacker@example.com | Reason: BadCredentialsException | IP: 203.0.113.42

[WARN]  ⊘ AUTHORIZATION DENIED | User: alice | Roles: [ROLE_READER] | Endpoint: GET /admin/users | IP: 192.168.1.100

[WARN]  ⚠ ACCOUNT LOCKED | User: test@example.com | Failed attempts: 5 | Lockout duration: 900000ms
```

---

## Security Best Practices

### 1. CORS Configuration

- ✅ Use specific origins (never use `*` with credentials)
- ✅ Limit allowed methods to only what's needed
- ✅ Specify explicit headers instead of wildcard
- ✅ Use environment variables for origin configuration
- ✅ Enable credentials only when necessary

### 2. JWT Security

- ✅ Store JWTs in localStorage/sessionStorage (not cookies)
- ✅ Use HTTPS to prevent token interception
- ✅ Implement short token expiry (24 hours)
- ✅ Validate token signature, expiry, and claims on every request
- ✅ Implement token blacklist for logout
- ✅ BCrypt password hashing with cost factor ≥ 12

### 3. Alternative Security Measures

- ✅ **XSS Protection**: Sanitize all user inputs (primary concern for JWT APIs)
- ✅ **Input Validation**: Validate all request data on server-side
- ✅ **HTTPS Only**: Never transmit tokens over HTTP
- ✅ **Rate Limiting**: Prevent brute force attacks with account lockout
- ✅ **Security Headers**: Use Content-Security-Policy, X-Frame-Options
- ✅ **Security Event Logging**: Monitor authentication and authorization events

---

## Environment Variables

### Security Configuration

```bash
# JWT Configuration
JWT_SECRET=your-secret-key-min-256-bits-long-for-hs256-algorithm
JWT_EXPIRATION=86400000  # 24 hours in milliseconds

# CORS Allowed Origins (comma-separated)
CORS_ORIGINS=http://localhost:3000,http://localhost:8080,https://your-frontend.com

# Server Configuration
PORT=8080

# Login Attempt Configuration
MAX_LOGIN_ATTEMPTS=5
LOGIN_LOCKOUT_DURATION_MS=900000  # 15 minutes

# Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_KRATOSGADO_BLOG=DEBUG
```

---

## Testing Security Configuration

### 1. Test Authentication

```bash
# Login with valid credentials
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@example.com",
    "password": "password123"
  }'

# Use token in subsequent request
curl -X GET http://localhost:8080/api/v1/posts \
  -H "Authorization: Bearer <token>"

# Invalid token (should fail)
curl -X GET http://localhost:8080/api/v1/posts \
  -H "Authorization: Bearer invalid-token"
```

### 2. Test Role-Based Access Control

```bash
# ADMIN endpoint with READER token (should fail)
curl -X GET http://localhost:8080/api/v1/admin/users \
  -H "Authorization: Bearer <reader-token>"

# AUTHOR endpoint with ADMIN token (should succeed)
curl -X GET http://localhost:8080/api/v1/author/posts \
  -H "Authorization: Bearer <admin-token>"
```

### 3. Test CORS

```bash
# Valid origin (success)
curl -X OPTIONS http://localhost:8080/api/v1/posts \
  -H "Origin: http://localhost:3000"

# Invalid origin (server allows, but browser blocks)
curl -X OPTIONS http://localhost:8080/api/v1/posts \
  -H "Origin: http://malicious-site.com"
```

### 4. Test Brute-Force Protection

```bash
# First 4 failed attempts
for i in {1..4}; do
  curl -X POST http://localhost:8080/api/v1/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"alice@example.com","password":"wrong"}'
done

# 5th attempt - should lock account
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"wrong"}'
# Response: 400 Bad Request (Account locked)
```

---

## Related Documentation

- [API Endpoints](ENDPOINTS.md) - Complete endpoint reference with security notes
- [Architecture Overview](ARCHITECTURE.md) - Security architecture diagram
- [Installation Guide](INSTALLATION.md) - Environment setup for security
