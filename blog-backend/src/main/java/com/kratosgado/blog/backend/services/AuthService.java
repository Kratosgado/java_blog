package com.kratosgado.blog.backend.services;

import com.kratosgado.blog.dtos.request.LoginRequest;
import com.kratosgado.blog.dtos.request.RegisterRequest;
import com.kratosgado.blog.dtos.response.AuthResponse;
import java.util.Map;

public interface AuthService {

  AuthResponse login(LoginRequest request);

  AuthResponse register(RegisterRequest request);

  Map<String, Object> logout(String authHeader);

  Map<String, Object> validateToken(String authHeader);
}
