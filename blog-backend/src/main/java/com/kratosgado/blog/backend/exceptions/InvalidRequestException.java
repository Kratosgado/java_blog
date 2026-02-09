package com.kratosgado.blog.backend.exceptions;

/**
 * Exception thrown when request parameters or body are invalid.
 */
public class InvalidRequestException extends BlogException {

  public InvalidRequestException(String message) {
    super(message);
  }

  public InvalidRequestException(String message, Throwable cause) {
    super(message, cause);
  }
}
