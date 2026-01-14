package com.kratosgado.blog.dtos.response;

public record AuthResponse(
  String token,
  Long userId,
  String username,
  String email,
  String role
) {}
