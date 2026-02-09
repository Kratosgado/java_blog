package com.kratosgado.blog.backend.exceptions;

/**
 * Base exception for all blog-related business logic errors.
 * Subclasses should represent specific error scenarios.
 */
public abstract class BlogException extends RuntimeException {

  protected BlogException(String message) {
    super(message);
  }

  protected BlogException(String message, Throwable cause) {
    super(message, cause);
  }

  protected BlogException(Throwable cause) {
    super(cause);
  }
}
