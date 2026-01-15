
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

}
