package com.kratosgado.blog.backend.exceptions;

/**
 * Exception thrown when authentication is required but missing or invalid.
 */
public class UnauthorizedException extends BlogException {

  public UnauthorizedException(String message) {
    super(message);
  }

  public UnauthorizedException() {
    super("Authentication required");
  }
}
