package com.kratosgado.blog.backend.graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

import com.kratosgado.blog.backend.security.JwtUtil;
import com.kratosgado.blog.backend.services.AuthService;
import com.kratosgado.blog.dtos.request.LoginRequest;
import com.kratosgado.blog.dtos.request.RegisterRequest;
import com.kratosgado.blog.dtos.response.AuthPayload;
import com.kratosgado.blog.models.User;

@Controller
public class AuthGraphQLController {

  private final AuthService authService;
  private final JwtUtil jwtUtil;

  public AuthGraphQLController(AuthService authService, JwtUtil jwtUtil) {
    this.authService = authService;
    this.jwtUtil = jwtUtil;
  }

  @MutationMapping
  public AuthPayload register(@Argument RegisterRequest input) {
    User user = authService.register(input);
    String token = jwtUtil.generateToken(user.getEmail(), user.getId());
    return new AuthPayload(token, user);
  }

  @MutationMapping
  public AuthPayload login(@Argument LoginRequest input) {
    User user = authService.login(input);
    String token = jwtUtil.generateToken(user.getEmail(), user.getId());
    return new AuthPayload(token, user);
  }
}
