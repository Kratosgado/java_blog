
package com.kratosgado.blog.utils.exceptions;

public class InternalException extends BlogException {
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
