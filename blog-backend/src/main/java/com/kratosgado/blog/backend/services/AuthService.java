package com.kratosgado.blog.backend.services;

import com.kratosgado.blog.backend.exceptions.InvalidRequestException;
import com.kratosgado.blog.backend.exceptions.ResourceAlreadyExistsException;
import com.kratosgado.blog.backend.exceptions.UnauthorizedException;
import com.kratosgado.blog.backend.repositories.jpa.UserRepository;
import com.kratosgado.blog.backend.security.JwtUtil;
import com.kratosgado.blog.dtos.request.LoginRequest;
import com.kratosgado.blog.dtos.request.RegisterRequest;
import com.kratosgado.blog.dtos.response.AuthResponse;
import com.kratosgado.blog.enums.UserRole;
import com.kratosgado.blog.backend.models.User;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
public class AuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final LoginAttemptService loginAttemptService;
  private final JwtUtil jwtUtil;
  private final TokenBlacklistService tokenBlacklistService;

  /**
   * Authenticate user with brute-force protection
   *
   * <p>
   * <b>Security Features:</b>
   *
   * <ul>
   * <li>Brute-force protection: Account lockout after max failed attempts
   * <li>Failed attempt tracking: Records all authentication failures
   * <li>Automatic unlock: Time-based recovery after lockout period
   * <li>Rate limiting: Prevents credential stuffing attacks
   * </ul>
   *
   * <p>
   * <b>Algorithm:</b> 1. Check if account is blocked (O(1) HashMap lookup) 2. If
   * blocked → throw
   * 429 Too Many Requests with remaining lockout time 3. If not blocked → proceed
   * with
   * authentication 4. On success → clear failed attempt history 5. On failure →
   * record failed
   * attempt (may trigger lockout)
   *
   * @param request Login credentials (email and password)
   * @return Authenticated User entity
   * @throws UnauthorizedException   if credentials are invalid (401)
   * @throws InvalidRequestException if account is locked (429)
   */
  public AuthResponse login(LoginRequest request) {
    String email = request.email();

    // STEP 1: Check if account is blocked due to too many failed attempts
    if (loginAttemptService.isBlocked(email)) {
      long remainingLockoutTime = loginAttemptService.getRemainingLockoutTime(email);
      int attemptCount = loginAttemptService.getAttemptCount(email);

      // HTTP 429 Too Many Requests is the appropriate status for rate limiting
      throw new InvalidRequestException(
          String.format(
              "Account temporarily locked due to too many failed login attempts. "
                  + "Please try again in %d seconds. (Failed attempts: %d)",
              remainingLockoutTime / 1000, attemptCount));
    }

    // STEP 2: Fetch user from database
    var user = userRepository
        .findBy(email)
        .orElseThrow(
            () -> {
              // User not found - record failed attempt to prevent username enumeration timing
              // attacks
              loginAttemptService.recordFailedAttempt(email);
              return new UnauthorizedException("Invalid email or password");
            });

    // STEP 3: Verify password
    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      // Password mismatch - record failed attempt
      loginAttemptService.recordFailedAttempt(email);

      // Log for security monitoring (SecurityEventListener will also log this)
      int attemptCount = loginAttemptService.getAttemptCount(email);
      if (attemptCount >= 3) {
        // Log warning after 3 failed attempts
        org.slf4j.LoggerFactory.getLogger(AuthService.class)
            .warn(
                "Multiple failed login attempts detected for: {} (attempt #{})",
                email,
                attemptCount);
      }

      throw new UnauthorizedException("Invalid email or password");
    }

    // STEP 4: Authentication successful - clear failed attempt history
    loginAttemptService.recordSuccessfulAttempt(email);

    return jwtUtil.signToken(user);
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public AuthResponse register(RegisterRequest request) {
    if (!request.password().equals(request.confirmPassword())) {
      throw new InvalidRequestException("Passwords do not match");
    }
    if (userRepository.findByEmail(request.email()).isPresent()) {
      throw new ResourceAlreadyExistsException("Email already exists");
    }

    var user = new User();
    user.setEmail(request.email());
    user.setUsername(request.username());
    user.setPassword(passwordEncoder.encode(request.password()));
    user.setAvatarUrl(request.avatarUrl());
    user.setRole(UserRole.READER); // Default role for new users
    userRepository.save(user);
    return jwtUtil.signToken(user);
  }

  public Map<String, Object> logout(String authHeader) {

    // Validate Authorization header format
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      throw new UnauthorizedException("Missing or invalid Authorization header");
    }

    // Extract JWT token
    String token = authHeader.substring(7);

    // Extract username and expiry from token
    String sub = jwtUtil.extractSub(token);
    long expiryTimestamp = jwtUtil.extractExpiration(token).getTime();

    // Add token to blacklist
    tokenBlacklistService.blacklistToken(token, expiryTimestamp);

    // Prepare response
    Map<String, Object> data = new HashMap<>();
    data.put("message", "Logout successful");
    data.put("sub", sub);
    data.put("tokenBlacklisted", true);
    data.put("tokenExpiresAt", expiryTimestamp);
    data.put("note", "This token can no longer be used for authentication");

    return data;
  }

  public Map<String, Object> validateToken(String authHeader) {
    try {
      if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String token = authHeader.substring(7);
        var payload = jwtUtil.extractPayload(token);

        if (jwtUtil.validateToken(token, payload.userId().toString())) {
          Map<String, Object> data = Map.of(
              "valid", true, "sub", payload.userId().toString(), "username", payload.username());
          return data;
        }
      }
    } catch (Exception e) {
    }
    return Map.of("valid", false);
  }
}
