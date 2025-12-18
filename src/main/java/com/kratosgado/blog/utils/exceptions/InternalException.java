
package com.kratosgado.blog.utils.exceptions;

public class InternalException extends RuntimeException {
  public InternalException(String message) {
    super(message);
  }

  public InternalException() {
    super("Internal Operation Failed");
  }

  public InternalException(Throwable cause) {
    super(cause);
  }

}
