package com.kratosgado.blog.backend.exceptions;

/**
 * Exception thrown when attempting to create a resource that already exists.
 */
public class ResourceAlreadyExistsException extends BlogException {

  public ResourceAlreadyExistsException(String message) {
    super(message);
  }

  public ResourceAlreadyExistsException(String resource, String field, Object value) {
    super(String.format("%s with %s '%s' already exists", resource, field, value));
  }
}
