package com.kratosgado.blog.backend.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;

@DisplayName("JwtUtil Tests")
class JwtUtilTest {

  private JwtUtil jwtUtil;
  private final String testSecret = "testSecretKeyForJwtTokenGenerationAndValidationMustBeLongEnough";
  private final Long testExpiration = 3600000L; // 1 hour

  @BeforeEach
  void setUp() {
    jwtUtil = new JwtUtil();
    ReflectionTestUtils.setField(jwtUtil, "secret", testSecret);
    ReflectionTestUtils.setField(jwtUtil, "expiration", testExpiration);
  }

  @Test
  @DisplayName("Should generate valid JWT token")
  void generateToken_WithValidData_ShouldReturnToken() {
    // Arrange
    String username = "testuser";
    Long userId = 1L;

    // Act
    String token = jwtUtil.generateToken(username, userId);

    // Assert
    assertNotNull(token);
    assertFalse(token.isEmpty());
    assertTrue(token.split("\\.").length == 3);
  }

  @Test
  @DisplayName("Should extract username from token")
  void extractUsername_WithValidToken_ShouldReturnUsername() {
    // Arrange
    String username = "testuser";
    Long userId = 1L;
    String token = jwtUtil.generateToken(username, userId);

    // Act
    String extractedUsername = jwtUtil.extractUsername(token);

    // Assert
    assertEquals(username, extractedUsername);
  }

  @Test
  @DisplayName("Should extract userId from token")
  void extractUserId_WithValidToken_ShouldReturnUserId() {
    // Arrange
    String username = "testuser";
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", 123L);
    String token = jwtUtil.generateToken(username, claims);

    // Act
    Integer userIdAsInt = jwtUtil.extractClaim(token, claimsObj -> {
      Object userIdObj = claimsObj.get("userId");
      if (userIdObj instanceof Double) {
        return ((Double) userIdObj).intValue();
      } else if (userIdObj instanceof Integer) {
        return (Integer) userIdObj;
      }
      return null;
    });

    // Assert
    assertNotNull(userIdAsInt);
    assertEquals(123, userIdAsInt);
  }

  @Test
  @DisplayName("Should extract expiration from token")
  void extractExpiration_WithValidToken_ShouldReturnExpiration() {
    // Arrange
    String username = "testuser";
    Long userId = 1L;
    String token = jwtUtil.generateToken(username, userId);

    // Act
    var expiration = jwtUtil.extractExpiration(token);

    // Assert
    assertNotNull(expiration);
    assertTrue(expiration.getTime() > System.currentTimeMillis());
  }

  @ParameterizedTest
  @MethodSource("validationTestCases")
  @DisplayName("Should validate token correctly")
  void validateToken_ShouldReturnExpectedResult(String tokenUsername, String validateUsername, boolean expected) {
    // Arrange
    Long userId = 1L;
    String token = jwtUtil.generateToken(tokenUsername, userId);

    // Act
    Boolean isValid = jwtUtil.validateToken(token, validateUsername);

    // Assert
    assertEquals(expected, isValid);
  }

  static Stream<Arguments> validationTestCases() {
    return Stream.of(
        Arguments.of("testuser", "testuser", true),
        Arguments.of("testuser", "wronguser", false));
  }

  @Test
  @DisplayName("Should generate token with custom claims")
  void generateToken_WithCustomClaims_ShouldIncludeClaims() {
    // Arrange
    String username = "testuser";
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", 1L);
    claims.put("role", "ADMIN");
    claims.put("email", "test@example.com");

    // Act
    String token = jwtUtil.generateToken(username, claims);

    // Assert
    assertNotNull(token);
    String extractedUsername = jwtUtil.extractUsername(token);
    String role = jwtUtil.extractClaim(token, claimsObj -> claimsObj.get("role", String.class));

    assertEquals(username, extractedUsername);
    assertEquals("ADMIN", role);
  }

  @Test
  @DisplayName("Should extract custom claim from token")
  void extractClaim_WithCustomClaim_ShouldReturnClaimValue() {
    // Arrange
    String username = "testuser";
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", 1L);
    claims.put("role", "ADMIN");
    String token = jwtUtil.generateToken(username, claims);

    // Act
    String role = jwtUtil.extractClaim(token, claimsObj -> claimsObj.get("role", String.class));

    // Assert
    assertEquals("ADMIN", role);
  }

  @ParameterizedTest
  @MethodSource("invalidTokenTestCases")
  @DisplayName("Should throw exception for invalid tokens")
  void invalidToken_ShouldThrowException(String testCase, String token, Class<? extends Exception> expectedException) {
    // Arrange - token provided by test case

    // Act & Assert
    assertThrows(expectedException, () -> jwtUtil.extractUsername(token));
  }

  static Stream<Arguments> invalidTokenTestCases() {
    return Stream.of(
        Arguments.of("malformed", "invalid.token.here", MalformedJwtException.class));
  }

  @Test
  @DisplayName("Should throw exception for expired token")
  void validateToken_WithExpiredToken_ShouldThrowException() {
    // Arrange
    JwtUtil shortExpirationJwtUtil = new JwtUtil();
    ReflectionTestUtils.setField(shortExpirationJwtUtil, "secret", testSecret);
    ReflectionTestUtils.setField(shortExpirationJwtUtil, "expiration", -1000L);

    String username = "testuser";
    Long userId = 1L;
    String expiredToken = shortExpirationJwtUtil.generateToken(username, userId);

    // Act & Assert
    assertThrows(ExpiredJwtException.class, () -> jwtUtil.extractUsername(expiredToken));
  }

  @Test
  @DisplayName("Should reject token with incorrect signature")
  void validateToken_WithWrongSignature_ShouldThrowException() {
    // Arrange
    String username = "testuser";
    Long userId = 1L;
    String token = jwtUtil.generateToken(username, userId);

    JwtUtil differentSecretJwtUtil = new JwtUtil();
    ReflectionTestUtils.setField(differentSecretJwtUtil, "secret",
        "differentSecretKeyThatIsAlsoVeryLongForJwtGeneration");
    ReflectionTestUtils.setField(differentSecretJwtUtil, "expiration", testExpiration);

    // Act & Assert
    assertThrows(Exception.class, () -> differentSecretJwtUtil.extractUsername(token));
  }

  @Test
  @DisplayName("Should generate different tokens for different users")
  void generateToken_ForDifferentUsers_ShouldReturnDifferentTokens() {
    // Arrange
    String username1 = "user1";
    String username2 = "user2";
    Long userId1 = 1L;
    Long userId2 = 2L;

    // Act
    String token1 = jwtUtil.generateToken(username1, userId1);
    String token2 = jwtUtil.generateToken(username2, userId2);

    // Assert
    assertNotEquals(token1, token2);
  }

  @Test
  @DisplayName("Should handle null claims gracefully")
  void extractUserId_WithMissingClaim_ShouldReturnNull() {
    // Arrange
    String username = "testuser";
    Map<String, Object> claims = new HashMap<>();
    String token = jwtUtil.generateToken(username, claims);

    // Act
    Long userId = jwtUtil.extractUserId(token);

    // Assert
    assertNull(userId);
  }
}
