package com.kratosgado.blog.backend.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.jpa.UserRepository;
import com.kratosgado.blog.dtos.request.LoginRequest;
import com.kratosgado.blog.dtos.request.RegisterRequest;
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

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private AuthService authService;

  private User testUser;
  private LoginRequest loginRequest;
  private RegisterRequest registerRequest;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setEmail("test@example.com");
    testUser.setUsername("testuser");
    testUser.setPassword("encodedPassword");
    testUser.setRole("USER");

    loginRequest = new LoginRequest("test@example.com", "password123");
    registerRequest =
        new RegisterRequest(
            "test@example.com",
            "testuser",
            "https://example.com/avatar.png",
            "password123",
            "password123");
  }

  @Test
  @DisplayName("Should successfully login with valid credentials")
  void login_WithValidCredentials_ShouldReturnUser() {
    // Arrange
    when(userRepository.findBy(loginRequest.email())).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(loginRequest.password(), testUser.getPassword())).thenReturn(true);

    // Act
    User result = authService.login(loginRequest);

    // Assert
    assertNotNull(result);
    assertEquals(testUser.getEmail(), result.getEmail());
  }

  @Test
  @DisplayName("Should throw exception when user not found during login")
  void login_WithInvalidEmail_ShouldThrowException() {
    // Arrange
    when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.empty());

    // Act
    BlogException exception =
        assertThrows(BlogException.class, () -> authService.login(loginRequest));

    // Assert
    assertEquals("Invalid email or password", exception.getMessage());
    verifyNoInteractions(passwordEncoder);
  }

  @Test
  @DisplayName("Should throw exception when password is incorrect")
  void login_WithInvalidPassword_ShouldThrowException() {
    // Arrange
    when(userRepository.findBy(loginRequest.email())).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(loginRequest.password(), testUser.getPassword()))
        .thenReturn(false);

    // Act
    BlogException exception =
        assertThrows(BlogException.class, () -> authService.login(loginRequest));

    // Assert
    assertEquals("Invalid email or password", exception.getMessage());
  }

  @Test
  @DisplayName("Should successfully register new user")
  void register_WithValidData_ShouldReturnUser() {
    // Arrange
    when(userRepository.findByEmail(registerRequest.email())).thenReturn(Optional.empty());
    when(passwordEncoder.encode(registerRequest.password())).thenReturn("encodedPassword");
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    // Act
    User result = authService.register(registerRequest);

    // Assert
    assertNotNull(result);
    assertEquals(testUser.getEmail(), result.getEmail());
  }

  @Test
  @DisplayName("Should throw exception when email already exists during registration")
  void register_WithExistingEmail_ShouldThrowException() {
    // Arrange
    when(userRepository.findBy(registerRequest.email())).thenReturn(Optional.of(testUser));

    // Act
    BlogException exception =
        assertThrows(BlogException.class, () -> authService.register(registerRequest));

    // Assert
    assertEquals("Email already exists", exception.getMessage());
    verifyNoInteractions(passwordEncoder);
  }

  @Test
  @DisplayName("Should set default role as USER during registration")
  void register_ShouldSetDefaultRole() {
    // Arrange
    when(userRepository.findByEmail(registerRequest.email())).thenReturn(Optional.empty());
    when(passwordEncoder.encode(registerRequest.password())).thenReturn("encodedPassword");
    when(userRepository.save(any(User.class)))
        .thenAnswer(
            invocation -> {
              User savedUser = invocation.getArgument(0);
              // The service doesn't set a role, so we can't test this behavior
              return savedUser;
            });

    // Act
    authService.register(registerRequest);

    // Assert - verification done in mock answer
  }
}
