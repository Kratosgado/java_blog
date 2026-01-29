package com.kratosgado.blog.backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.jdbc.UserRepository;
import com.kratosgado.blog.dtos.request.PageRequest;
import com.kratosgado.blog.models.User;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private BCryptPasswordEncoder passwordEncoder;

  @InjectMocks
  private UserService userService;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setEmail("test@example.com");
    testUser.setUsername("testuser");
    testUser.setPassword("encodedPassword");
    testUser.setRole("USER");
    testUser.setBio("Test bio");
  }

  @ParameterizedTest
  @MethodSource("getUserByIdTestCases")
  @DisplayName("Should get user by ID with or without password")
  void getUserById_ShouldReturnUserBasedOnPasswordFlag(boolean includePassword, boolean expectPassword)
      throws SQLException {
    // Arrange
    when(userRepository.findById(eq(1L))).thenReturn(Optional.of(testUser));

    // Act
    User result = includePassword ? userService.getUserById(1L, true) : userService.getUserById(1L);

    // Assert
    assertNotNull(result);
    assertEquals(testUser.getId(), result.getId());
    if (expectPassword) {
      assertEquals("encodedPassword", result.getPassword());
    } else {
      assertNull(result.getPassword());
    }
  }

  static Stream<Arguments> getUserByIdTestCases() {
    return Stream.of(
        Arguments.of(false, false), // without password flag
        Arguments.of(true, true) // with password flag
    );
  }

  @ParameterizedTest
  @MethodSource("getUserNotFoundTestCases")
  @DisplayName("Should throw exception when user not found")
  void getUserNotFound_ShouldThrowException(String operation) throws SQLException {
    // Arrange
    switch (operation) {
      case "byId":
        when(userRepository.findById(eq(1L))).thenReturn(Optional.empty());
        break;
      case "byEmail":
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        break;
      case "byUsername":
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        break;
    }

    // Act & Assert
    BlogException exception;
    switch (operation) {
      case "byId":
        exception = assertThrows(BlogException.class, () -> userService.getUserById(1L));
        break;
      case "byEmail":
        exception = assertThrows(BlogException.class,
            () -> userService.getUserByEmail("nonexistent@example.com"));
        break;
      case "byUsername":
        exception = assertThrows(BlogException.class,
            () -> userService.getUserByUsername("nonexistent"));
        break;
      default:
        throw new IllegalArgumentException("Unknown operation: " + operation);
    }
    assertTrue(exception.getMessage().contains("User not found"));
  }

  static Stream<Arguments> getUserNotFoundTestCases() {
    return Stream.of(
        Arguments.of("byId"),
        Arguments.of("byEmail"),
        Arguments.of("byUsername"));
  }

  @Test
  @DisplayName("Should get user by email")
  void getUserByEmail_WithValidEmail_ShouldReturnUser() throws SQLException {
    // Arrange
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

    // Act
    User result = userService.getUserByEmail("test@example.com");

    // Assert
    assertNotNull(result);
    assertEquals(testUser.getEmail(), result.getEmail());
  }

  @Test
  @DisplayName("Should get user by username")
  void getUserByUsername_WithValidUsername_ShouldReturnUser() throws SQLException {
    // Arrange
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

    // Act
    User result = userService.getUserByUsername("testuser");

    // Assert
    assertNotNull(result);
    assertEquals(testUser.getUsername(), result.getUsername());
  }

  @Test
  @DisplayName("Should get all users")
  void getAllUsers_ShouldReturnPageOfUsers() {
    // Arrange
    PageRequest pageRequest = PageRequest.builder().page(0).size(10).sortBy("id").sortDir("desc").build();
    java.util.List<User> users = java.util.List.of(testUser);
    when(userRepository.findAll(eq(10), eq(0), eq("id"), eq("desc"))).thenReturn(users);
    when(userRepository.count()).thenReturn(1L);

    // Act
    var result = userService.getAllUsers(pageRequest);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.totalElements());
  }

  static Stream<Arguments> unauthorizedOperationTestCases() {
    return Stream.of(
        Arguments.of("updateAvatar"),
        Arguments.of("changePassword"));
  }
}
