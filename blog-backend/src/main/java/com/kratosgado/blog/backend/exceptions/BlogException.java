
package com.kratosgado.blog.backend.exceptions;

import org.springframework.http.HttpStatus;

public class BlogException extends RuntimeException {
  private final HttpStatus status;

  public BlogException(String message, HttpStatus status) {
    super(message);
    this.status = status;
  }

  public BlogException(Throwable cause) {
    super(cause);
    this.status = null;
  }

  public HttpStatus getStatus() {
    return status;
  }

  // Factory methods with simple message
  public static BlogException badRequest(String message) {
    return new BlogException(message, HttpStatus.BAD_REQUEST);
  }

  public static BlogException conflict(String message) {
    return new BlogException(message, HttpStatus.CONFLICT);
  }

  public static BlogException notFound(String message) {
    return new BlogException(message, HttpStatus.NOT_FOUND);
  }

  public static BlogException internal(String message) {
    return new BlogException(message, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  public static BlogException internal() {
    return new BlogException("Internal Operation Failed", HttpStatus.INTERNAL_SERVER_ERROR);
  }

  public static BlogException unauthorized(String message) {
    return new BlogException(message, HttpStatus.UNAUTHORIZED);
  }

  public static BlogException forbidden(String message) {
    return new BlogException(message, HttpStatus.FORBIDDEN);
  }

  // Factory methods with resource/field/value parameters
  public static BlogException notFound(String resource, String field, Object value) {
    return new BlogException(
        String.format("%s not found with %s: '%s'", resource, field, value),
        HttpStatus.NOT_FOUND);
  }

  public static BlogException conflict(String resource, String field, Object value) {
    return new BlogException(
        String.format("%s with %s '%s' already exists", resource, field, value),
        HttpStatus.CONFLICT);
  }

  public static BlogException duplicateResource(String message) {
    return new BlogException(message, HttpStatus.CONFLICT);
  }

  public static BlogException duplicateResource(String resource, String field, Object value) {
    return conflict(resource, field, value);
  }
}
