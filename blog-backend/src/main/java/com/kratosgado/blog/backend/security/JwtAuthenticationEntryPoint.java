package com.kratosgado.blog.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kratosgado.blog.dtos.response.ResponseDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException, ServletException {

    log.warn(
        "Authentication failed for request: {} {} - Reason: {}",
        request.getMethod(),
        request.getRequestURI(),
        authException.getMessage());

    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setStatus(HttpStatus.UNAUTHORIZED.value());

    ResponseDto<?> errorResponse =
        ResponseDto.error(
            HttpStatus.UNAUTHORIZED.value(),
            "Authentication required. Please provide a valid JWT token.");

    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
  }
}
