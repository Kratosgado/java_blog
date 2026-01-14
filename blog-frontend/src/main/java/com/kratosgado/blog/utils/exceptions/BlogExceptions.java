
package com.kratosgado.blog.utils.exceptions;

public class BlogExceptions {

  public static BadRequestException badRequest(String message) {
    return new BadRequestException(message);
  }

  public static ConflictException conflict(String message) {
    return new ConflictException(message);
  }

  public static NotFoundException notFound(String message) {
    return new NotFoundException(message);
  }

  public static InternalException internal(String message) {
    return new InternalException(message);
  }

  public static InternalException internal() {
    return new InternalException();
  }

  public static UnAuthorizedException unauthorized(String message) {
    return new UnAuthorizedException(message);
  }

}
