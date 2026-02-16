package com.kratosgado.blog.backend.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import com.kratosgado.blog.dtos.response.ResponseDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Map;
import java.util.Set;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  @DisplayName("handleResourceNotFound should return 404")
  void handleResourceNotFound_shouldReturn404() {
    ResponseEntity<ResponseDto<?>> response =
        handler.handleResourceNotFound(new ResourceNotFoundException("Missing"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  @DisplayName("handleResourceAlreadyExists should return 409")
  void handleResourceAlreadyExists_shouldReturn409() {
    ResponseEntity<ResponseDto<?>> response =
        handler.handleResourceAlreadyExists(new ResourceAlreadyExistsException("Exists"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  @DisplayName("handleUnauthorized should return 401")
  void handleUnauthorized_shouldReturn401() {
    ResponseEntity<ResponseDto<?>> response =
        handler.handleUnauthorized(new UnauthorizedException("Nope"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  @DisplayName("handleForbidden should return 403")
  void handleForbidden_shouldReturn403() {
    ResponseEntity<ResponseDto<?>> response =
        handler.handleForbidden(new ForbiddenException("No access"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  @DisplayName("handleInvalidRequest should return 400")
  void handleInvalidRequest_shouldReturn400() {
    ResponseEntity<ResponseDto<?>> response =
        handler.handleInvalidRequest(new InvalidRequestException("bad"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  @DisplayName("handleAuthenticationException should return 401")
  void handleAuthenticationException_shouldReturn401() {
    AuthenticationException ex = new AuthenticationException("auth") {};
    ResponseEntity<ResponseDto<?>> response = handler.handleAuthenticationException(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  @DisplayName("handleAccessDenied should return 403")
  void handleAccessDenied_shouldReturn403() {
    ResponseEntity<ResponseDto<?>> response =
        handler.handleAccessDenied(new AccessDeniedException("denied"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  @DisplayName("handleRateLimit should return 429")
  void handleRateLimit_shouldReturn429() {
    io.github.resilience4j.ratelimiter.RequestNotPermitted ex =
        org.mockito.Mockito.mock(io.github.resilience4j.ratelimiter.RequestNotPermitted.class);
    ResponseEntity<?> response = handler.handleRateLimit(ex);

    assertThat(((ResponseDto<?>) response.getBody()).status())
        .isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
  }

  @Test
  @DisplayName("handleIllegalArgument should return 400")
  void handleIllegalArgument_shouldReturn400() {
    ResponseEntity<ResponseDto<?>> response =
        handler.handleIllegalArgument(new IllegalArgumentException("bad"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  @DisplayName("handleIllegalState should return 409")
  void handleIllegalState_shouldReturn409() {
    ResponseEntity<ResponseDto<?>> response =
        handler.handleIllegalState(new IllegalStateException("bad"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  @DisplayName("handleTypeMismatch should return 400")
  void handleTypeMismatch_shouldReturn400() {
    MethodArgumentTypeMismatchException ex =
        new MethodArgumentTypeMismatchException("v", Integer.class, "param", null, null);
    ResponseEntity<ResponseDto<?>> response = handler.handleTypeMismatch(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  @DisplayName("handleMissingParameter should return 400")
  void handleMissingParameter_shouldReturn400() {
    MissingServletRequestParameterException ex =
        new MissingServletRequestParameterException("p", "String");

    ResponseEntity<ResponseDto<?>> response = handler.handleMissingParameter(ex);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  @DisplayName("handleRuntimeException should return 500")
  void handleRuntimeException_shouldReturn500() {
    WebRequest request = org.mockito.Mockito.mock(WebRequest.class);
    ResponseEntity<ResponseDto<?>> response =
        handler.handleRuntimeException(new RuntimeException("x"), request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  @DisplayName("handleGlobalException should return 500")
  void handleGlobalException_shouldReturn500() {
    WebRequest request = org.mockito.Mockito.mock(WebRequest.class);
    ResponseEntity<ResponseDto<?>> response =
        handler.handleGlobalException(new Exception("x"), request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
