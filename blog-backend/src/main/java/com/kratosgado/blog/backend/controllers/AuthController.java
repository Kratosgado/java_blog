package com.kratosgado.blog.backend.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kratosgado.blog.backend.annotations.OpenApi.CreateEndpoint;
import com.kratosgado.blog.backend.security.JwtUtil;
import com.kratosgado.blog.backend.services.AuthService;
import com.kratosgado.blog.dtos.request.LoginRequest;
import com.kratosgado.blog.dtos.request.RegisterRequest;
import com.kratosgado.blog.dtos.response.AuthResponse;
import com.kratosgado.blog.dtos.response.ResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService authService;
  private final JwtUtil jwtUtil;

  @PostMapping("/login")
  @CreateEndpoint
  public AuthResponse login(@Valid @RequestBody LoginRequest request) {
    var user = authService.login(request);

    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", user.getId());
    claims.put("role", user.getRole());

    String token = jwtUtil.generateToken(user.getEmail(), claims);

    return new AuthResponse(
        token,
        user.getId().longValue(),
        user.getUsername(),
        user.getEmail(),
        user.getRole());

  }

  @PostMapping("/register")
  @CreateEndpoint
  public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
    var user = authService.register(request);

    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", user.getId());
    claims.put("role", user.getRole());

    String token = jwtUtil.generateToken(user.getEmail(), claims);

    return new AuthResponse(
        token,
        user.getId().longValue(),
        user.getUsername(),
        user.getEmail(),
        user.getRole());

  }

  @GetMapping("/validate")
  public ResponseDto<Map<String, Object>> validateToken(
      @RequestHeader("Authorization") String authHeader) {
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
}
