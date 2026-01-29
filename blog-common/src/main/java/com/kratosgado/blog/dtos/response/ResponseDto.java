package com.kratosgado.blog.dtos.response;

public record ResponseDto<T>(
    int status,
    String message,
    T data) {
  public static <T> ResponseDto<T> success(T data) {
    return success(200, "Operation completed successfully", data);
  }

  public static <T> ResponseDto<T> success(String message, T data) {
    return success(200, message, data);
  }

  public static <T> ResponseDto<T> success(int status, String message, T data) {
    return new ResponseDto<>(status, message, data);
  }

  public static <T> ResponseDto<T> error(String message) {
    return new ResponseDto<>(500, message, null);
  }

  public static <T> ResponseDto<T> error(int status, String message) {
    return new ResponseDto<>(status, message, null);
  }

  public static <T> ResponseDto<T> fail(String message, T data) {
    return fail(400, message, data);
  }

  public static <T> ResponseDto<T> fail(int status, String message, T data) {
    return new ResponseDto<>(status, message, data);
  }
}
