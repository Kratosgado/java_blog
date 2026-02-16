package com.kratosgado.blog.backend.controllers.v1;

import com.kratosgado.blog.backend.annotations.OpenApi.GetEndpoint;
import com.kratosgado.blog.backend.annotations.OpenApi.SecuredUpdateEndpoint;
import com.kratosgado.blog.backend.annotations.OpenApi.UpdateEndpoint;
import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.services.AuthService;
import com.kratosgado.blog.dtos.request.LoginRequest;
import com.kratosgado.blog.dtos.request.RegisterRequest;
import com.kratosgado.blog.dtos.response.AuthResponse;
import com.kratosgado.blog.dtos.response.ResponseDto;
import com.kratosgado.blog.enums.UserRole;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

  @PostMapping("/login")
  @UpdateEndpoint(
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
    return authService.login(request);
  }

  @PostMapping("/register")
  @UpdateEndpoint(
      summary = "User Registration",
      description =
          "Register a new user account with email, username, and password. Automatically generates"
              + " and returns JWT token for immediate authentication. New users are assigned READER"
              + " role by default.")
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
    return authService.register(request);
  }

  @GetMapping("/validate")
  @GetEndpoint(
      summary = "Validate JWT Token",
      description =
          "Validates a JWT token and returns whether it's valid. Checks token signature,"
              + " expiration, and structure. Returns username if token is valid. Does not check"
              + " blacklist status.")
  public ResponseDto<Map<String, Object>> validateToken(
      @Parameter(
              description = "Authorization header with Bearer token",
              required = true,
              example = "Bearer eyJhbGc...")
          @RequestHeader("Authorization")
          String authHeader) {
    return ResponseDto.success(authService.validateToken(authHeader));
  }

  @PostMapping("/logout")
  @SecuredUpdateEndpoint(
      summary = "User Logout",
      description =
          "Blacklists JWT token to prevent reuse. Token is added to in-memory blacklist until its"
              + " natural expiration. Subsequent requests with this token will be rejected with 401"
              + " Unauthorized.",
      roles = {UserRole.READER, UserRole.AUTHOR, UserRole.ADMIN})
  public Map<String, Object> logout(
      @Parameter(
              description = "Authorization header with Bearer token",
              required = false,
              example = "Bearer eyJhbGc...")
          @RequestHeader(value = "Authorization", required = false)
          String authHeader)
      throws BlogException {
    return authService.logout(authHeader);
  }
}
