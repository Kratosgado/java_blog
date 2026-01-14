package com.kratosgado.blog.backend.controllers;

import com.kratosgado.blog.dtos.request.LoginRequest;
import com.kratosgado.blog.dtos.request.RegisterRequest;
import com.kratosgado.blog.dtos.response.ApiResponse;
import com.kratosgado.blog.dtos.response.AuthResponse;
import com.kratosgado.blog.backend.security.JwtUtil;
import com.kratosgado.blog.backend.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

  private final AuthService authService;
  private final JwtUtil jwtUtil;

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
    logger.info("Login attempt for email: {}", request.email());

    var user = authService.login(request);

    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", user.getId());
    claims.put("role", user.getRole());

    String token = jwtUtil.generateToken(user.getEmail(), claims);

    AuthResponse authResponse = new AuthResponse(
        token,
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getRole());

    return ResponseEntity.ok(ApiResponse.success(authResponse));
  }

  @PostMapping("/register")
  public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
    logger.info("Registration attempt for email: {}", request.email());

    var user = authService.register(request);

    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", user.getId());
    claims.put("role", user.getRole());

    String token = jwtUtil.generateToken(user.getEmail(), claims);

    AuthResponse authResponse = new AuthResponse(
        token,
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getRole());

    return ResponseEntity.ok(ApiResponse.success(authResponse));
  }

  @GetMapping("/validate")
  public ResponseEntity<ApiResponse<Map<String, Object>>> validateToken(
      @RequestHeader("Authorization") String authHeader) {
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7);
      String username = jwtUtil.extractUsername(token);

      if (jwtUtil.validateToken(token, username)) {
        Map<String, Object> data = Map.of("valid", true, "username", username);
        return ResponseEntity.ok(ApiResponse.success(data));
      }
    }
    return ResponseEntity.ok(ApiResponse.success(Map.of("valid", false)));
  }
}
