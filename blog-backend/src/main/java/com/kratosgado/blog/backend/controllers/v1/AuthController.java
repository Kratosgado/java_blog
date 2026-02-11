package com.kratosgado.blog.backend.controllers.v1;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.exceptions.InvalidRequestException;
import com.kratosgado.blog.backend.exceptions.UnauthorizedException;
import com.kratosgado.blog.backend.security.JwtUtil;
import com.kratosgado.blog.backend.services.AuthService;
import com.kratosgado.blog.backend.services.TokenBlacklistService;
import com.kratosgado.blog.dtos.request.LoginRequest;
import com.kratosgado.blog.dtos.request.RegisterRequest;
import com.kratosgado.blog.dtos.response.AuthResponse;
import com.kratosgado.blog.dtos.response.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Authentication APIs")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;
  private final JwtUtil jwtUtil;
  private final TokenBlacklistService tokenBlacklistService;

  @PostMapping("/login")
  @Operation(
      summary = "User Login",
      description =
          "Authenticate user with email and password. Returns JWT token signed with HS256"
              + " algorithm. Token includes user ID, email, and roles. Protected against"
              + " brute-force attacks with account lockout after 5 failed attempts.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Login successful, JWT token returned"),
    @ApiResponse(
        responseCode = "400",
        description =
            "Bad request - Invalid input or account locked due to too many failed login attempts"),
    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid email or password")
  })
  public AuthResponse login(
      @Valid
          @RequestBody
          @Parameter(description = "Login credentials (email and password)", required = true)
          LoginRequest request)
      throws BlogException {
    var user = authService.login(request);

    // Build JWT claims with RBAC role
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", user.getId());
    claims.put("role", user.getRoleString()); // Include role: ADMIN, AUTHOR, READER

    // Generate JWT token with claims
    // Subject: user email
    // Claims: userId, role
    // Issued-at and expiry are automatically added by JwtUtil
    String token = jwtUtil.generateToken(user.getEmail(), claims);

    return new AuthResponse(
        token, user.getId().longValue(), user.getUsername(), user.getEmail(), user.getRoleString());
  }

  /**
   * User Registration with JWT Token
   *
   * <p>Registers new user and immediately returns JWT token for seamless login.
   *
   * @param request Registration details (email, username, password)
   * @return AuthResponse with JWT token and user details
   */
  @PostMapping("/register")
  @Operation(
      summary = "User Registration",
      description =
          "Register a new user account with email, username, and password. "
              + "Automatically generates and returns JWT token for immediate authentication. "
              + "New users are assigned READER role by default.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "User created successfully, JWT token returned"),
    @ApiResponse(
        responseCode = "400",
        description = "Bad request - Invalid input or user already exists")
  })
  @ResponseStatus(HttpStatus.CREATED)
  public AuthResponse register(
      @Valid @RequestBody @Parameter(description = "User registration details", required = true)
          RegisterRequest request) {
    var user = authService.register(request);

    // Build JWT claims with RBAC role
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", user.getId());
    claims.put("role", user.getRole().name()); // Include role: ADMIN, AUTHOR, READER

    String token = jwtUtil.generateToken(user.getEmail(), claims);

    return new AuthResponse(
        token,
        user.getId().longValue(),
        user.getUsername(),
        user.getEmail(),
        user.getRole().name());
  }

  @GetMapping("/validate")
  @Operation(
      summary = "Validate JWT Token",
      description =
          "Validates a JWT token and returns whether it's valid. "
              + "Checks token signature, expiration, and structure. "
              + "Returns username if token is valid. Does not check blacklist status.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Validation result returned (valid: true/false)")
  })
  public ResponseDto<Map<String, Object>> validateToken(
      @Parameter(
              description = "Authorization header with Bearer token",
              required = true,
              example = "Bearer eyJhbGc...")
          @RequestHeader("Authorization")
          String authHeader) {
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7);
      String username = jwtUtil.extractUsername(token);

      if (jwtUtil.validateToken(token, username)) {
        Map<String, Object> data = Map.of("valid", true, "username", username);
        return ResponseDto.success(data);
      }
    }
    return ResponseDto.success(Map.of("valid", false));
  }

  /**
   * Logout endpoint - Blacklists JWT token to prevent reuse
   *
   * <p><b>Security Mechanism:</b>
   *
   * <ul>
   *   <li>Extracts JWT token from Authorization header
   *   <li>Retrieves token expiration timestamp
   *   <li>Adds token to blacklist with expiry time
   *   <li>Token will be rejected by JwtAuthenticationFilter on subsequent use
   * </ul>
   *
   * <p><b>DSA Analysis:</b>
   *
   * <ul>
   *   <li>Blacklist operation: O(1) - HashMap put
   *   <li>Expiry extraction: O(1) - JWT claims parsing
   * </ul>
   *
   * <p><b>Design Decision:</b> Tokens are blacklisted until their natural expiry (not
   * indefinitely). This balances security with memory efficiency.
   *
   * @param authHeader Authorization header containing Bearer token
   * @return Success message with logout confirmation
   * @throws BlogException if Authorization header is missing or invalid
   */
  @PostMapping("/logout")
  @Operation(
      summary = "User Logout",
      description =
          "Blacklists JWT token to prevent reuse. Token is added to in-memory blacklist "
              + "until its natural expiration. Blacklist operations are O(1) time complexity. "
              + "Subsequent requests with this token will be rejected with 401 Unauthorized.",
      security = @SecurityRequirement(name = "bearer-jwt"))
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Logout successful, token blacklisted"),
    @ApiResponse(responseCode = "400", description = "Bad request - Invalid token format"),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - Missing or invalid Authorization header")
  })
  public ResponseDto<Map<String, Object>> logout(
      @Parameter(
              description = "Authorization header with Bearer token",
              required = true,
              example = "Bearer eyJhbGc...")
          @RequestHeader("Authorization")
          String authHeader)
      throws BlogException {

    // Validate Authorization header format
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      throw new UnauthorizedException("Missing or invalid Authorization header");
    }

    // Extract JWT token
    String token = authHeader.substring(7);

    try {
      // Extract username and expiry from token
      String username = jwtUtil.extractUsername(token);
      long expiryTimestamp = jwtUtil.extractExpiration(token).getTime();

      // Add token to blacklist
      tokenBlacklistService.blacklistToken(token, expiryTimestamp);

      // Prepare response
      Map<String, Object> data = new HashMap<>();
      data.put("message", "Logout successful");
      data.put("username", username);
      data.put("tokenBlacklisted", true);
      data.put("tokenExpiresAt", expiryTimestamp);
      data.put("note", "This token can no longer be used for authentication");

      return ResponseDto.success(data);

    } catch (Exception e) {
      throw new InvalidRequestException("Invalid token: " + e.getMessage());
    }
  }

  @GetMapping("google")
  public Map<String, Object> google() {
    return Map.of("message", "Google OAuth2 callback");
  }
}
