package com.kratosgado.blog.backend.exceptions;

/**
 * Exception thrown when user lacks permission to access a resource.
 */
public class ForbiddenException extends BlogException {

  public ForbiddenException(String message) {
    super(message);
  }

  public ForbiddenException() {
    super("Access denied");
  }
}
