package com.kratosgado.blog.dtos.response;

import com.kratosgado.blog.models.User;

public record AuthPayload(
  String token,
  User user
) {}
