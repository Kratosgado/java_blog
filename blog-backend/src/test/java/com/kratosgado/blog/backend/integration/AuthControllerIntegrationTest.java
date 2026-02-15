package com.kratosgado.blog.backend.integration;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.kratosgado.blog.backend.repositories.jpa.UserRepository;
import com.kratosgado.blog.dtos.request.LoginRequest;
import com.kratosgado.blog.dtos.request.RegisterRequest;
import com.kratosgado.blog.enums.UserRole;
import com.kratosgado.blog.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Integration tests for AuthController.
 * Tests authentication endpoints including login, registration, token
 * validation, and logout.
 */
@DisplayName("AuthController Integration Tests")
class AuthControllerIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  private User testUser;
  private final String TEST_PASSWORD = "@Password123";
  private final String TEST_EMAIL = "test@example.com";

  @BeforeEach
  @Override
  void baseSetUp() {
    // Clean up database
    userRepository.deleteAll();

    // Create test user
    testUser = new User();
    testUser.setEmail(TEST_EMAIL);
    testUser.setUsername("testuser");
    testUser.setPassword(passwordEncoder.encode(TEST_PASSWORD));
    testUser.setRole(UserRole.READER);
    testUser.setAvatarUrl("https://example.com/avatar.png");
    testUser = userRepository.save(testUser);
  }

  @Nested
  @DisplayName("Login Tests")
  class LoginTests {

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void login_WithValidCredentials_ShouldReturnToken() throws Exception {
      LoginRequest request = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);

      mockMvc.perform(post("/v1/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(toJson(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.token", notNullValue()))
          .andExpect(jsonPath("$.data.email", is(TEST_EMAIL)))
          .andExpect(jsonPath("$.data.username", is("testuser")))
          .andExpect(jsonPath("$.data.role", is("READER")));
    }

    @Test
    @DisplayName("Should return 401 for invalid email")
    void login_WithInvalidEmail_ShouldReturn401() throws Exception {
      LoginRequest request = new LoginRequest("invalid@example.com", TEST_PASSWORD);

      mockMvc.perform(post("/v1/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(toJson(request)))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message", containsString("Invalid email or password")));
    }

    @Test
    @DisplayName("Should return 401 for invalid password")
    void login_WithInvalidPassword_ShouldReturn401() throws Exception {
      LoginRequest request = new LoginRequest(TEST_EMAIL, "@1Wrongpassword");

      mockMvc.perform(post("/v1/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(toJson(request)))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.message", containsString("Invalid email or password")));
    }

    @ParameterizedTest
    @DisplayName("Should return 400 for invalid email format")
    @ValueSource(strings = { "", "invalid-email", "test@", "@example.com", "test.example.com" })
    void login_WithInvalidEmailFormat_ShouldReturn400(String invalidEmail) throws Exception {
      LoginRequest request = new LoginRequest(invalidEmail, TEST_PASSWORD);

      mockMvc.perform(post("/v1/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(toJson(request)))
          .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @DisplayName("Should return 400 for invalid password")
    @ValueSource(strings = { "", " ", "short" })
    void login_WithInvalidPassword_ShouldReturn400(String invalidPassword) throws Exception {
      LoginRequest request = new LoginRequest(TEST_EMAIL, invalidPassword);

      mockMvc.perform(post("/v1/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(toJson(request)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("Registration Tests")
  class RegistrationTests {

    @Test
    @DisplayName("Should successfully register new user")
    void register_WithValidData_ShouldReturn201() throws Exception {
      RegisterRequest request = new RegisterRequest(
          "newuser",
          "newuser@example.com",
          "https://example.com/new-avatar.png",
          "@Password123",
          "@Password123");

      mockMvc.perform(post("/v1/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(toJson(request)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.data.token", notNullValue()))
          .andExpect(jsonPath("$.data.email", is("newuser@example.com")))
          .andExpect(jsonPath("$.data.username", is("newuser")))
          .andExpect(jsonPath("$.data.role", is("READER")));
    }

    @Test
    @DisplayName("Should return 400 when email already exists")
    void register_WithExistingEmail_ShouldReturn400() throws Exception {
      RegisterRequest request = new RegisterRequest(
          "anotheruser",
          TEST_EMAIL, // Already exists
          "https://example.com/avatar.png",
          "@Password123",
          "@Password123");

      mockMvc.perform(post("/v1/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(toJson(request)))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.message", containsString("Email already exists")));
    }

    @Test
    @DisplayName("Should return 400 when passwords don't match")
    void register_WithMismatchedPasswords_ShouldReturn400() throws Exception {
      RegisterRequest request = new RegisterRequest(
          "newuser@example.com",
          "newuser",
          "https://example.com/avatar.png",
          "password123",
          "differentpassword");

      mockMvc.perform(post("/v1/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(toJson(request)))
          .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @CsvSource({
        "'', user1, https://example.com/avatar.png, pass123, pass123",
        "user@test.com, '', https://example.com/avatar.png, pass123, pass123",
        "user@test.com, ab, https://example.com/avatar.png, pass123, pass123", // Username too short
        "user@test.com, user1, https://example.com/avatar.png, '', ''", // Empty password
        "user@test.com, user1, https://example.com/avatar.png, short, short" // Password too short
    })
    @DisplayName("Should return 400 for invalid registration data")
    void register_WithInvalidData_ShouldReturn400(
        String email, String username, String avatarUrl, String password, String confirmPassword)
        throws Exception {
      RegisterRequest request = new RegisterRequest(email, username, avatarUrl, password, confirmPassword);

      mockMvc.perform(post("/v1/auth/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(toJson(request)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("Token Validation Tests")
  class TokenValidationTests {

    @Test
    @DisplayName("Should successfully validate valid token")
    void validateToken_WithValidToken_ShouldReturnValid() throws Exception {
      String token = generateToken(testUser.getId(), testUser.getEmail());

      mockMvc.perform(get("/v1/auth/validate")
          .header("Authorization", token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.valid", is(true)));
    }

    @Test
    @DisplayName("Should return invalid for expired/malformed token")
    void validateToken_WithInvalidToken_ShouldReturnInvalid() throws Exception {
      String invalidToken = "Bearer invalid.token.here";

      mockMvc.perform(get("/v1/auth/validate")
          .header("Authorization", invalidToken))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.valid", is(false)));
    }

  }

  @Nested
  @DisplayName("Logout Tests")
  class LogoutTests {

    @Test
    @DisplayName("Should successfully logout with valid token")
    void logout_WithValidToken_ShouldReturn200() throws Exception {
      String token = generateToken(testUser.getId(), testUser.getEmail());

      mockMvc.perform(post("/v1/auth/logout")
          .header("Authorization", token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message", containsString("Operation completed successfully")));
    }

    @Test
    @DisplayName("Should return 401 when token is missing")
    void logout_WithoutToken_ShouldReturn401() throws Exception {
      mockMvc.perform(post("/v1/auth/logout"))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Subsequent requests with blacklisted token should fail")
    void logout_BlacklistedToken_ShouldNotBeUsable() throws Exception {
      String token = generateToken(testUser.getId(), testUser.getEmail());

      // First logout
      mockMvc.perform(post("/v1/auth/logout")
          .header("Authorization", token))
          .andExpect(status().isOk());

      // Try to use the same token - should fail
      mockMvc.perform(get("/v1/auth/validate")
          .header("Authorization", token))
          .andExpect(status().isUnauthorized());
    }
  }
}
