package com.kratosgado.blog.backend.exceptions;

import com.kratosgado.blog.dtos.response.ResponseDto;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(BlogException.class)
  public ResponseEntity<ResponseDto<?>> handleBlogException(
      BlogException ex) {
    logger.error("Blog exception: {}", ex.getMessage());
    return ResponseEntity
        .status(ex.getStatus())
        .body(ResponseDto.error(ex.getStatus().value(), ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ResponseDto<Map<String, String>>> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
    logger.error("Validation error: {}", errors);
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ResponseDto.fail(HttpStatus.BAD_REQUEST.value(), "Validation failed", errors));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ResponseDto<?>> handleConstraintViolation(
      ConstraintViolationException ex) {
    String errors = ex.getConstraintViolations().stream()
        .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
        .collect(Collectors.joining(", "));
    logger.error("Constraint violation: {}", errors);
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ResponseDto.error(HttpStatus.BAD_REQUEST.value(), "Constraint violation: " + errors));
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ResponseDto<?>> handleAuthenticationException(
      AuthenticationException ex) {
    logger.error("Authentication failed: {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
        .body(
            ResponseDto.error(HttpStatus.UNAUTHORIZED.value(), "Authentication failed: " + ex.getMessage()));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ResponseDto<String>> handleAccessDenied(
      AccessDeniedException ex) {
    logger.error("Access denied: {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.FORBIDDEN)
        .body(ResponseDto.error(HttpStatus.FORBIDDEN.value(), "Access denied: " + ex.getMessage()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<?> handleIllegalArgument(
      IllegalArgumentException ex) {
    logger.error("Illegal argument: {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ResponseDto.error(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ResponseDto<?>> handleIllegalState(
      IllegalStateException ex) {
    logger.error("Illegal state: {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.CONFLICT)
        .body(ResponseDto.error(HttpStatus.CONFLICT.value(), ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ResponseDto<?>> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex) {
    String error = String.format("Parameter '%s' should be of type %s",
        ex.getName(),
        ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
    logger.error("Type mismatch: {}", error);
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ResponseDto.error(HttpStatus.BAD_REQUEST.value(), error));
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ResponseDto<String>> handleRuntimeException(
      RuntimeException ex, WebRequest request) {
    logger.error("Runtime exception at {}: {}", request.getDescription(false), ex.getMessage(), ex);
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ResponseDto.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ResponseDto<?>> handleGlobalException(
      Exception ex, WebRequest request) {
    logger.error("Unhandled exception at {}: {}", request.getDescription(false), ex.getMessage(), ex);
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            ResponseDto.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An internal server error occurred"));
  }
}
