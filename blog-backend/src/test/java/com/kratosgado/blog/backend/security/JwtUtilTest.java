package com.kratosgado.blog.backend.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.kratosgado.blog.backend.models.User;
import com.kratosgado.blog.dtos.response.AuthResponse;
import com.kratosgado.blog.enums.UserRole;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("JwtUtil Tests")
class JwtUtilTest {

  private JwtUtil jwtUtil;
  private final String testSecret =
      "testSecretKeyForJwtTokenGenerationAndValidationMustBeLongEnough";
  private final Long testExpiration = 3600000L; // 1 hour

  @BeforeEach
  void setUp() {
    jwtUtil = new JwtUtil(testSecret, testExpiration);
  }

  @Test
  @DisplayName("Should throw exception for short secret key")
  void constructor_WithShortSecret_ShouldThrowException() {
    // Arrange
    String shortSecret = "short";

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> new JwtUtil(shortSecret, testExpiration));
  }

  @Test
  @DisplayName("Should generate valid JWT token for user")
  void signToken_WithValidUser_ShouldReturnAuthResponse() {
    // Arrange
    User user = new User();
    user.setId(1L);
    user.setUsername("testuser");
    user.setEmail("test@example.com");
    user.setRole(UserRole.READER);

    // Act
    AuthResponse response = jwtUtil.signToken(user);

    // Assert
    assertNotNull(response);
    assertNotNull(response.token());
    assertFalse(response.token().isEmpty());
    assertEquals(3, response.token().split("\\.").length);
    assertEquals(1L, response.userId());
    assertEquals("testuser", response.username());
    assertEquals("test@example.com", response.email());
    assertEquals("READER", response.role());
  }

  @Test
  @DisplayName("Should extract sub from token")
  void extractSub_WithValidToken_ShouldReturnSub() {
    // Arrange
    User user = new User();
    user.setId(1L);
    user.setUsername("testuser");
    user.setEmail("test@example.com");
    user.setRole(UserRole.READER);
    String token = jwtUtil.signToken(user).token();

    // Act
    String extractedSub = jwtUtil.extractSub(token);

    // Assert
    assertEquals("1", extractedSub);
  }

  @Test
  @DisplayName("Should extract expiration from token")
  void extractExpiration_WithValidToken_ShouldReturnExpiration() {
    // Arrange
    User user = new User();
    user.setId(1L);
    user.setUsername("testuser");
    user.setEmail("test@example.com");
    user.setRole(UserRole.READER);
    String token = jwtUtil.signToken(user).token();

    // Act
    var expiration = jwtUtil.extractExpiration(token);

    // Assert
    assertNotNull(expiration);
    assertTrue(expiration.getTime() > System.currentTimeMillis());
  }

  @ParameterizedTest
  @MethodSource("validationTestCases")
  @DisplayName("Should validate token correctly")
  void validateToken_ShouldReturnExpectedResult(Long userId, String validateSub, boolean expected) {
    // Arrange
    User user = new User();
    user.setId(userId);
    user.setUsername("testuser");
    user.setEmail("test@example.com");
    user.setRole(UserRole.READER);
    String token = jwtUtil.signToken(user).token();

    // Act
    boolean isValid = jwtUtil.validateToken(token, validateSub);

    // Assert
    assertEquals(expected, isValid);
  }

  static Stream<Arguments> validationTestCases() {
    return Stream.of(Arguments.of(1L, "1", true), Arguments.of(1L, "2", false));
  }

  @Test
  @DisplayName("Should extract payload from token")
  void extractPayload_WithValidToken_ShouldReturnPayload() {
    // Arrange
    User user = new User();
    user.setId(123L);
    user.setUsername("testuser");
    user.setEmail("user@example.com");
    user.setRole(UserRole.ADMIN);
    String token = jwtUtil.signToken(user).token();

    // Act
    JwtUtil.JwtPayload payload = jwtUtil.extractPayload(token);

    // Assert
    assertNotNull(payload);
    assertEquals("user@example.com", payload.email());
    assertEquals(123L, payload.userId());
    assertEquals("ADMIN", payload.role());
  }

  @ParameterizedTest
  @MethodSource("invalidTokenTestCases")
  @DisplayName("Should throw exception for invalid tokens")
  void invalidToken_ShouldThrowException(
      String testCase, String token, Class<? extends Exception> expectedException) {
    // Act & Assert
    assertThrows(expectedException, () -> jwtUtil.extractSub(token));
  }

  static Stream<Arguments> invalidTokenTestCases() {
    return Stream.of(Arguments.of("malformed", "invalid.token.here", MalformedJwtException.class));
  }

  @Test
  @DisplayName("Should throw exception for expired token")
  void validateToken_WithExpiredToken_ShouldThrowException() {
    // Arrange
    JwtUtil shortExpirationJwtUtil = new JwtUtil(testSecret, -1000L);
    User user = new User();
    user.setId(1L);
    user.setUsername("testuser");
    user.setEmail("test@example.com");
    user.setRole(UserRole.READER);
    String expiredToken = shortExpirationJwtUtil.signToken(user).token();

    // Act & Assert
    assertThrows(ExpiredJwtException.class, () -> jwtUtil.extractSub(expiredToken));
  }

  @Test
  @DisplayName("Should reject token with incorrect signature")
  void validateToken_WithWrongSignature_ShouldThrowException() {
    // Arrange
    User user = new User();
    user.setId(1L);
    user.setUsername("testuser");
    user.setEmail("test@example.com");
    user.setRole(UserRole.READER);
    String token = jwtUtil.signToken(user).token();

    JwtUtil differentSecretJwtUtil =
        new JwtUtil("differentSecretKeyThatIsAlsoVeryLongForJwtGeneration", testExpiration);

    // Act & Assert
    assertThrows(Exception.class, () -> differentSecretJwtUtil.extractSub(token));
  }

  @Test
  @DisplayName("Should generate different tokens for different users")
  void generateToken_ForDifferentUsers_ShouldReturnDifferentTokens() {
    // Arrange
    User user1 = new User();
    user1.setId(1L);
    user1.setUsername("user1");
    user1.setEmail("user1@example.com");
    user1.setRole(UserRole.READER);

    User user2 = new User();
    user2.setId(2L);
    user2.setUsername("user2");
    user2.setEmail("user2@example.com");
    user2.setRole(UserRole.READER);

    // Act
    String token1 = jwtUtil.signToken(user1).token();
    String token2 = jwtUtil.signToken(user2).token();

    // Assert
    assertNotEquals(token1, token2);
  }

  @Test
  @DisplayName("Should validate token returns false for mismatched subject")
  void validateToken_WithMismatchedSubject_ShouldReturnFalse() {
    // Arrange
    User user = new User();
    user.setId(1L);
    user.setUsername("testuser");
    user.setEmail("test@example.com");
    user.setRole(UserRole.READER);
    String token = jwtUtil.signToken(user).token();

    // Act
    boolean isValid = jwtUtil.validateToken(token, "999");

    // Assert
    assertFalse(isValid);
  }

  @Test
  @DisplayName("Should include role in token claims")
  void signToken_ShouldIncludeRoleInClaims() {
    // Arrange
    User adminUser = new User();
    adminUser.setId(1L);
    adminUser.setUsername("admin");
    adminUser.setEmail("admin@example.com");
    adminUser.setRole(UserRole.ADMIN);

    // Act
    String token = jwtUtil.signToken(adminUser).token();
    JwtUtil.JwtPayload payload = jwtUtil.extractPayload(token);

    // Assert
    assertEquals("ADMIN", payload.role());
  }
}
