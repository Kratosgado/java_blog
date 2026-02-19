package com.kratosgado.blog.backend.exceptions;

import com.kratosgado.blog.dtos.response.ResponseDto;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.validation.ConstraintViolationException;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Global exception handler for all REST API exceptions. Maps domain exceptions to appropriate HTTP
 * responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ResponseDto<?>> handleResourceNotFound(ResourceNotFoundException ex) {
    logger.error("Resource not found: {}", ex.getMessage());
    return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(org.springframework.data.rest.webmvc.ResourceNotFoundException.class)
  public ResponseEntity<ResponseDto<?>> handleResourceNotFound(
      org.springframework.data.rest.webmvc.ResourceNotFoundException ex) {
    logger.error("Resource not found: {}", ex.getMessage());
    return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(ResourceAlreadyExistsException.class)
  public ResponseEntity<ResponseDto<?>> handleResourceAlreadyExists(
      ResourceAlreadyExistsException ex) {
    logger.error("Resource conflict: {}", ex.getMessage());
    return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ResponseDto<?>> handleUnauthorized(UnauthorizedException ex) {
    logger.error("Unauthorized access: {}", ex.getMessage());
    return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ResponseDto<?>> handleForbidden(ForbiddenException ex) {
    logger.error("Forbidden access: {}", ex.getMessage());
    return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage());
  }

  @ExceptionHandler(InvalidRequestException.class)
  public ResponseEntity<ResponseDto<?>> handleInvalidRequest(InvalidRequestException ex) {
    logger.error("Invalid request: {}", ex.getMessage());
    return buildErrorResponse(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ResponseDto<?>> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    Map<String, String> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .collect(
                Collectors.toMap(
                    error -> error.getField(),
                    error ->
                        error.getDefaultMessage() != null
                            ? error.getDefaultMessage()
                            : "Invalid value",
                    (existing, replacement) -> existing));

    logger.error("Validation failed: {}", errors);
    return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed", errors);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ResponseDto<?>> handleConstraintViolation(ConstraintViolationException ex) {
    Map<String, String> errors =
        ex.getConstraintViolations().stream()
            .collect(
                Collectors.toMap(
                    violation -> violation.getPropertyPath().toString(),
                    violation -> violation.getMessage(),
                    (existing, replacement) -> existing));

    logger.error("Constraint violation: {}", errors);
    return buildErrorResponse(HttpStatus.BAD_REQUEST, "Constraint violation", errors);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ResponseDto<?>> handleAuthenticationException(AuthenticationException ex) {
    logger.error("Authentication failed: {}", ex.getMessage());
    return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Authentication failed");
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ResponseDto<?>> handleAccessDenied(AccessDeniedException ex) {
    logger.error("Access denied: {}", ex.getMessage());
    return buildErrorResponse(HttpStatus.FORBIDDEN, "Access denied");
  }

  @ExceptionHandler(RequestNotPermitted.class)
  public ResponseEntity<?> handleRateLimit(RequestNotPermitted ex) {
    return buildErrorResponse(HttpStatus.TOO_MANY_REQUESTS, "Too many requests");
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ResponseDto<?>> handleIllegalArgument(IllegalArgumentException ex) {
    logger.error("Invalid argument: {}", ex.getMessage());
    return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ResponseDto<?>> handleIllegalState(IllegalStateException ex) {
    logger.error("Invalid state: {}", ex.getMessage());
    return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ResponseDto<?>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    String message =
        String.format(
            "Parameter '%s' should be of type %s",
            ex.getName(),
            ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

    logger.error("Type mismatch: {}", message);
    return buildErrorResponse(HttpStatus.BAD_REQUEST, message);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ResponseDto<?>> handleMissingParameter(
      MissingServletRequestParameterException ex) {
    String message = String.format("Required parameter '%s' is missing", ex.getParameterName());

    logger.error("Missing parameter: {}", message);
    return buildErrorResponse(HttpStatus.BAD_REQUEST, message);
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ResponseDto<?>> handleRuntimeException(
      RuntimeException ex, WebRequest request) {
    logger.error("Runtime exception at {}: {}", request.getDescription(false), ex.getMessage(), ex);
    return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ResponseDto<?>> handleGlobalException(Exception ex, WebRequest request) {
    logger.error(
        "Unhandled exception at {}: {}", request.getDescription(false), ex.getMessage(), ex);
    return buildErrorResponse(
        HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred");
  }

  /** Builds a standardized error response. */
  private ResponseEntity<ResponseDto<?>> buildErrorResponse(HttpStatus status, String message) {
    return ResponseEntity.status(status).body(ResponseDto.error(status.value(), message));
  }

  /** Builds a standardized error response with validation errors. */
  private ResponseEntity<ResponseDto<?>> buildErrorResponse(
      HttpStatus status, String message, Map<String, String> errors) {
    return ResponseEntity.status(status).body(ResponseDto.error(status.value(), message, errors));
  }
}
