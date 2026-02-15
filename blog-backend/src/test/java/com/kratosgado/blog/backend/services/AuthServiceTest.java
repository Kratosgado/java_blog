package com.kratosgado.blog.backend.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.kratosgado.blog.backend.exceptions.InvalidRequestException;
import com.kratosgado.blog.backend.exceptions.ResourceAlreadyExistsException;
import com.kratosgado.blog.backend.exceptions.UnauthorizedException;
import com.kratosgado.blog.backend.repositories.jpa.UserRepository;
import com.kratosgado.blog.backend.security.JwtUtil;
import com.kratosgado.blog.dtos.request.LoginRequest;
import com.kratosgado.blog.dtos.request.RegisterRequest;
import com.kratosgado.blog.dtos.response.AuthResponse;
import com.kratosgado.blog.dtos.response.UserResponse;
import com.kratosgado.blog.enums.UserRole;
import com.kratosgado.blog.models.User;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private JwtUtil jwtUtil;

  @Mock
  private LoginAttemptService loginAttemptService;

  @Mock
  private TokenBlacklistService tokenBlacklistService;

  @InjectMocks
  private AuthService authService;

  private User testUser;
  private AuthResponse testAuthResponse;
  private LoginRequest loginRequest;
  private RegisterRequest registerRequest;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setEmail("test@example.com");
    testUser.setUsername("testuser");
    testUser.setPassword("encodedPassword");
    testUser.setRole(UserRole.READER);

    testAuthResponse = new AuthResponse(
        "mock-jwt-token",
        1L,
        "testuser",
        "test@example.com",
        "READER");

    loginRequest = new LoginRequest("test@example.com", "password123");
    registerRequest = new RegisterRequest(
        "test@example.com",
        "testuser",
        "https://example.com/avatar.png",
        "password123",
        "password123");
  }

  @Test
  @DisplayName("Should successfully login with valid credentials")
  void login_WithValidCredentials_ShouldReturnAuthResponse() {
    // Arrange
    when(loginAttemptService.isBlocked(loginRequest.email())).thenReturn(false);
    when(userRepository.findBy(loginRequest.email())).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(loginRequest.password(),
        testUser.getPassword())).thenReturn(true);
    when(jwtUtil.signToken(testUser)).thenReturn(testAuthResponse);

    // Act
    var result = authService.login(loginRequest);

    // Assert
    assertNotNull(result);
    assertEquals(testUser.getEmail(), result.email());
    assertEquals(testUser.getUsername(), result.username());
  }

  @Test
  @DisplayName("Should throw exception when user not found during login")
  void login_WithInvalidEmail_ShouldThrowException() {
    // Arrange
    when(loginAttemptService.isBlocked(loginRequest.email())).thenReturn(false);
    when(userRepository.findBy(loginRequest.email())).thenReturn(Optional.empty());

    // Act
    UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> authService.login(loginRequest));

    // Assert
    assertEquals("Invalid email or password", exception.getMessage());
    verifyNoInteractions(passwordEncoder);
  }

  @Test
  @DisplayName("Should throw exception when password is incorrect")
  void login_WithInvalidPassword_ShouldThrowException() {
    // Arrange
    when(loginAttemptService.isBlocked(loginRequest.email())).thenReturn(false);
    when(userRepository.findBy(loginRequest.email())).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(loginRequest.password(), testUser.getPassword()))
        .thenReturn(false);

    // Act
    UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> authService.login(loginRequest));

    // Assert
    assertEquals("Invalid email or password", exception.getMessage());
  }

  @Test
  @DisplayName("Should successfully register new user")
  void register_WithValidData_ShouldReturnAuthResponse() {
    // Arrange
    when(userRepository.findByEmail(registerRequest.email())).thenReturn(Optional.empty());
    when(passwordEncoder.encode(registerRequest.password())).thenReturn("encodedPassword");
    when(userRepository.save(any(User.class))).thenReturn(testUser);
    when(jwtUtil.signToken(any(User.class))).thenReturn(testAuthResponse);

    // Act
    var result = authService.register(registerRequest);

    // Assert
    assertNotNull(result);
    assertEquals(testUser.getEmail(), result.email());
    assertEquals(testUser.getUsername(), result.username());
  }

  @Test
  @DisplayName("Should throw exception when email already exists during registration")
  void register_WithExistingEmail_ShouldThrowException() {
    // Arrange
    UserResponse existingUser = org.mockito.Mockito.mock(UserResponse.class);
    when(userRepository.findByEmail(registerRequest.email())).thenReturn(Optional.of(existingUser));

    // Act
    ResourceAlreadyExistsException exception = assertThrows(ResourceAlreadyExistsException.class,
        () -> authService.register(registerRequest));

    // Assert
    assertEquals("Email already exists", exception.getMessage());
    verifyNoInteractions(passwordEncoder);
  }

  @Test
  @DisplayName("Should set default role as READER during registration")
  void register_ShouldSetDefaultRole() {
    // Arrange
    when(userRepository.findByEmail(registerRequest.email())).thenReturn(Optional.empty());
    when(passwordEncoder.encode(registerRequest.password())).thenReturn("encodedPassword");
    when(userRepository.save(any(User.class)))
        .thenAnswer(
            invocation -> {
              User savedUser = invocation.getArgument(0);
              assertEquals(UserRole.READER, savedUser.getRole(), "Default role should be READER");
              return savedUser;
            });
    when(jwtUtil.signToken(any(User.class))).thenReturn(testAuthResponse);

    // Act
    authService.register(registerRequest);

    // Assert - verification done in mock answer
  }

  @Test
  @DisplayName("Should throw exception when account is blocked during login")
  void login_WithBlockedAccount_ShouldThrowException() {
    // Arrange
    when(loginAttemptService.isBlocked(loginRequest.email())).thenReturn(true);
    when(loginAttemptService.getRemainingLockoutTime(loginRequest.email())).thenReturn(300000L); // 5 minutes
    when(loginAttemptService.getAttemptCount(loginRequest.email())).thenReturn(5);

    // Act
    InvalidRequestException exception = assertThrows(InvalidRequestException.class,
        () -> authService.login(loginRequest));

    // Assert
    assertTrue(exception.getMessage().contains("Account temporarily locked"));
    assertTrue(exception.getMessage().contains("300 seconds"));
    verifyNoInteractions(userRepository);
    verifyNoInteractions(passwordEncoder);
  }

  @Test
  @DisplayName("Should throw exception when passwords do not match during registration")
  void register_WithMismatchedPasswords_ShouldThrowException() {
    // Arrange
    RegisterRequest mismatchedRequest = new RegisterRequest(
        "test@example.com",
        "testuser",
        "https://example.com/avatar.png",
        "password123",
        "password456");

    // Act
    InvalidRequestException exception = assertThrows(InvalidRequestException.class,
        () -> authService.register(mismatchedRequest));

    // Assert
    assertEquals("Passwords do not match", exception.getMessage());
    verifyNoInteractions(userRepository);
    verifyNoInteractions(passwordEncoder);
  }
}
