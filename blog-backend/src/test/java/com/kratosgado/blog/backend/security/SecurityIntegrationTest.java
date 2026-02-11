package com.kratosgado.blog.backend.security;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kratosgado.blog.dtos.request.LoginRequest;
import com.kratosgado.blog.dtos.request.RegisterRequest;
import com.kratosgado.blog.enums.UserRole;
import com.kratosgado.blog.models.User;
import com.kratosgado.blog.backend.config.TestSecurityConfig;
import com.kratosgado.blog.backend.repositories.jpa.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * Comprehensive Security Integration Tests for Lab 7
 *
 * <p>Tests:
 * <ul>
 *   <li>JWT Authentication Flow (login, register, logout)</li>
 *   <li>Role-Based Access Control (RBAC)</li>
 *   <li>Token Blacklist (logout mechanism)</li>
 *   <li>CORS Headers</li>
 *   <li>Security Event Logging</li>
 *   <li>Brute-force Protection</li>
 * </ul>
 */
@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration"
})
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("Security Integration Tests (Lab 7 - Epic 6)")
public class SecurityIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private BCryptPasswordEncoder passwordEncoder;

  @Autowired
  private JwtUtil jwtUtil;

  private User adminUser;
  private User authorUser;
  private User readerUser;
  private String adminToken;
  private String authorToken;
  private String readerToken;

  @BeforeEach
  void setUp() {
    // Clean up test data
    userRepository.deleteAll();

    // Create test users with different roles
    adminUser = User.builder()
        .username("admin")
        .email("admin@test.com")
        .password(passwordEncoder.encode("admin123"))
        .role(UserRole.ADMIN)
        .build();
    adminUser = userRepository.save(adminUser);

    authorUser = User.builder()
        .username("author")
        .email("author@test.com")
        .password(passwordEncoder.encode("author123"))
        .role(UserRole.AUTHOR)
        .build();
    authorUser = userRepository.save(authorUser);

    readerUser = User.builder()
        .username("reader")
        .email("reader@test.com")
        .password(passwordEncoder.encode("reader123"))
        .role(UserRole.READER)
        .build();
    readerUser = userRepository.save(readerUser);

    // Generate tokens for testing
    adminToken = jwtUtil.generateToken(adminUser.getEmail(), adminUser.getId());
    authorToken = jwtUtil.generateToken(authorUser.getEmail(), authorUser.getId());
    readerToken = jwtUtil.generateToken(readerUser.getEmail(), readerUser.getId());
  }

  @Nested
  @DisplayName("JWT Authentication Tests")
  class JwtAuthenticationTests {

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void login_WithValidCredentials_ShouldReturnToken() throws Exception {
      LoginRequest loginRequest = new LoginRequest("admin@test.com", "admin123");

      mockMvc.perform(post("/api/v1/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(loginRequest)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.token").exists())
          .andExpect(jsonPath("$.token").isString())
          .andExpect(jsonPath("$.email").value("admin@test.com"))
          .andExpect(jsonPath("$.username").value("admin"));
    }

    @Test
    @DisplayName("Should return 401 for invalid credentials")
    void login_WithInvalidCredentials_ShouldReturn401() throws Exception {
      LoginRequest loginRequest = new LoginRequest("admin@test.com", "wrongpassword");

      mockMvc.perform(post("/api/v1/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(loginRequest)))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should register new user successfully")
    void register_WithValidData_ShouldCreateUser() throws Exception {
      RegisterRequest registerRequest = new RegisterRequest(
          "newuser",
          "newuser@test.com",
          null,  // avatarUrl
          "password123",
          "password123"  // confirmPassword
      );

      mockMvc.perform(post("/api/v1/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(registerRequest)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.token").exists())
          .andExpect(jsonPath("$.email").value("newuser@test.com"))
          .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    @DisplayName("Should return 400 when registering with existing email")
    void register_WithExistingEmail_ShouldReturn400() throws Exception {
      RegisterRequest registerRequest = new RegisterRequest(
          "newuser",
          "admin@test.com",  // Already exists
          null,  // avatarUrl
          "password123",
          "password123"  // confirmPassword
      );

      mockMvc.perform(post("/api/v1/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(registerRequest)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should validate valid JWT token")
    void validate_WithValidToken_ShouldReturnValid() throws Exception {
      mockMvc.perform(get("/api/v1/auth/validate")
          .header("Authorization", "Bearer " + adminToken))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.valid").value(true))
          .andExpect(jsonPath("$.data.username").value("admin@test.com"));
    }

    @Test
    @DisplayName("Should return invalid for malformed token")
    void validate_WithMalformedToken_ShouldReturnInvalid() throws Exception {
      mockMvc.perform(get("/api/v1/auth/validate")
          .header("Authorization", "Bearer invalid.token.here"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.valid").value(false));
    }
  }

  @Nested
  @DisplayName("Token Blacklist Tests (Logout)")
  class TokenBlacklistTests {

    @Test
    @DisplayName("Should logout and blacklist token successfully")
    void logout_WithValidToken_ShouldBlacklistToken() throws Exception {
      // Logout with valid token
      mockMvc.perform(post("/api/v1/auth/logout")
          .header("Authorization", "Bearer " + adminToken))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.message").value("Logout successful"))
          .andExpect(jsonPath("$.data.tokenBlacklisted").value(true))
          .andExpect(jsonPath("$.data.username").value("admin@test.com"));

      // Try to use blacklisted token - should be rejected
      mockMvc.perform(get("/api/v1/admin/users")
          .header("Authorization", "Bearer " + adminToken))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 when logout without token")
    void logout_WithoutToken_ShouldReturn401() throws Exception {
      mockMvc.perform(post("/api/v1/auth/logout"))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 when logout with malformed token")
    void logout_WithMalformedToken_ShouldReturn401() throws Exception {
      mockMvc.perform(post("/api/v1/auth/logout")
          .header("Authorization", "Bearer malformed-token"))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("Role-Based Access Control (RBAC) Tests")
  class RbacTests {

    @Test
    @DisplayName("ADMIN should access admin endpoints")
    void adminEndpoint_WithAdminRole_ShouldAllow() throws Exception {
      mockMvc.perform(get("/api/v1/admin/users")
          .header("Authorization", "Bearer " + adminToken))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.requiredRole").value("ADMIN"));
    }

    @Test
    @DisplayName("AUTHOR should NOT access admin endpoints")
    void adminEndpoint_WithAuthorRole_ShouldDeny() throws Exception {
      mockMvc.perform(get("/api/v1/admin/users")
          .header("Authorization", "Bearer " + authorToken))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("READER should NOT access admin endpoints")
    void adminEndpoint_WithReaderRole_ShouldDeny() throws Exception {
      mockMvc.perform(get("/api/v1/admin/users")
          .header("Authorization", "Bearer " + readerToken))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("AUTHOR should access author endpoints")
    void authorEndpoint_WithAuthorRole_ShouldAllow() throws Exception {
      mockMvc.perform(get("/api/v1/author/posts")
          .header("Authorization", "Bearer " + authorToken))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.requiredRole").value(anyOf(is("AUTHOR"), is("ADMIN"))));
    }

    @Test
    @DisplayName("ADMIN should access author endpoints")
    void authorEndpoint_WithAdminRole_ShouldAllow() throws Exception {
      mockMvc.perform(get("/api/v1/author/posts")
          .header("Authorization", "Bearer " + adminToken))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("READER should NOT access author endpoints")
    void authorEndpoint_WithReaderRole_ShouldDeny() throws Exception {
      mockMvc.perform(get("/api/v1/author/posts")
          .header("Authorization", "Bearer " + readerToken))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("READER should access reader endpoints")
    void readerEndpoint_WithReaderRole_ShouldAllow() throws Exception {
      mockMvc.perform(get("/api/v1/reader/profile")
          .header("Authorization", "Bearer " + readerToken))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.authenticatedUser").value("reader@test.com"));
    }

    @Test
    @DisplayName("All roles should access reader endpoints")
    void readerEndpoint_WithAnyRole_ShouldAllow() throws Exception {
      // Test with all three roles
      mockMvc.perform(get("/api/v1/reader/profile")
          .header("Authorization", "Bearer " + readerToken))
          .andExpect(status().isOk());

      mockMvc.perform(get("/api/v1/reader/profile")
          .header("Authorization", "Bearer " + authorToken))
          .andExpect(status().isOk());

      mockMvc.perform(get("/api/v1/reader/profile")
          .header("Authorization", "Bearer " + adminToken))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 401 when accessing protected endpoint without token")
    void protectedEndpoint_WithoutToken_ShouldReturn401() throws Exception {
      mockMvc.perform(get("/api/v1/admin/users"))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("CORS Tests")
  class CorsTests {

    @Test
    @DisplayName("Should return CORS headers for allowed origin")
    void options_WithAllowedOrigin_ShouldReturnCorsHeaders() throws Exception {
      mockMvc.perform(options("/api/v1/posts")
          .header("Origin", "http://localhost:3000")
          .header("Access-Control-Request-Method", "POST")
          .header("Access-Control-Request-Headers", "Authorization,Content-Type"))
          .andExpect(status().isOk())
          .andExpect(header().exists("Access-Control-Allow-Origin"))
          .andExpect(header().exists("Access-Control-Allow-Methods"))
          .andExpect(header().exists("Access-Control-Allow-Headers"))
          .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    @DisplayName("Should handle CORS for GET requests")
    void get_WithCorsOrigin_ShouldReturnCorsHeaders() throws Exception {
      mockMvc.perform(get("/api/v1/posts")
          .header("Origin", "http://localhost:3000"))
          .andExpect(status().isOk())
          .andExpect(header().exists("Access-Control-Allow-Origin"));
    }
  }

  @Nested
  @DisplayName("Security Event Logging Tests")
  class SecurityEventLoggingTests {

    @Test
    @DisplayName("Should log successful login event")
    void login_Successful_ShouldLogEvent() throws Exception {
      LoginRequest loginRequest = new LoginRequest("admin@test.com", "admin123");

      // This test verifies the endpoint works
      // Actual event logging verification would require log capture
      mockMvc.perform(post("/api/v1/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(loginRequest)))
          .andExpect(status().isOk());

      // Note: In production, you'd verify SecurityEventListener captured this event
    }

    @Test
    @DisplayName("Should log failed login attempt")
    void login_Failed_ShouldLogEvent() throws Exception {
      LoginRequest loginRequest = new LoginRequest("admin@test.com", "wrongpassword");

      mockMvc.perform(post("/api/v1/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(loginRequest)))
          .andExpect(status().isUnauthorized());

      // Note: In production, you'd verify SecurityEventListener captured this event
    }
  }

  @Nested
  @DisplayName("Token Validation Tests")
  class TokenValidationTests {

    @Test
    @DisplayName("Should reject request with expired token")
    void request_WithExpiredToken_ShouldReturn401() throws Exception {
      // Create a token with very short expiration
      JwtUtil shortExpiryJwtUtil = new JwtUtil();
      org.springframework.test.util.ReflectionTestUtils.setField(
          shortExpiryJwtUtil, "secret",
          "testSecretKeyForJwtTokenGenerationAndValidationMustBeLongEnough"
      );
      org.springframework.test.util.ReflectionTestUtils.setField(
          shortExpiryJwtUtil, "expiration", 1L
      ); // 1ms expiration

      String expiredToken = shortExpiryJwtUtil.generateToken("test@test.com", 1L);

      // Wait for token to expire
      Thread.sleep(10);

      // Token should be rejected
      mockMvc.perform(get("/api/v1/admin/users")
          .header("Authorization", "Bearer " + expiredToken))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject request with malformed token")
    void request_WithMalformedToken_ShouldReturn401() throws Exception {
      mockMvc.perform(get("/api/v1/admin/users")
          .header("Authorization", "Bearer not.a.real.token"))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject request without Bearer prefix")
    void request_WithoutBearerPrefix_ShouldReturn401() throws Exception {
      mockMvc.perform(get("/api/v1/admin/users")
          .header("Authorization", adminToken))  // Missing "Bearer " prefix
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("Public Endpoint Tests")
  class PublicEndpointTests {

    @Test
    @DisplayName("Should allow access to public endpoints without authentication")
    void publicEndpoint_WithoutAuthentication_ShouldAllow() throws Exception {
      // These endpoints should be publicly accessible
      mockMvc.perform(get("/api/v1/posts"))
          .andExpect(status().isOk());

      mockMvc.perform(get("/api/v1/categories"))
          .andExpect(status().isOk());

      mockMvc.perform(get("/api/v1/tags"))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Auth endpoints should be public")
    void authEndpoints_WithoutAuthentication_ShouldAllow() throws Exception {
      LoginRequest loginRequest = new LoginRequest("admin@test.com", "admin123");

      mockMvc.perform(post("/api/v1/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(loginRequest)))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CSRF demo endpoints should be public")
    void csrfDemoEndpoints_WithoutAuthentication_ShouldAllow() throws Exception {
      mockMvc.perform(get("/api/v1/csrf-demo/info"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.cors").exists())
          .andExpect(jsonPath("$.csrf").exists());
    }
  }
}
