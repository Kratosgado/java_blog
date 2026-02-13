package com.kratosgado.blog.backend.graphql;

import com.kratosgado.blog.backend.services.AuthService;
import com.kratosgado.blog.dtos.request.LoginRequest;
import com.kratosgado.blog.dtos.request.RegisterRequest;
import com.kratosgado.blog.dtos.response.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class AuthGraphQLController {

  private final AuthService authService;

  @MutationMapping
  public AuthResponse register(@Argument RegisterRequest input) {
    return authService.register(input);
  }

  @MutationMapping
  public AuthResponse login(@Argument LoginRequest input) {
    return authService.login(input);
  }
}
