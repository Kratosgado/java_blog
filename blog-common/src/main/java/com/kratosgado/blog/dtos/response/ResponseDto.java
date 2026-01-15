package com.kratosgado.blog.dtos.response;

public record ResponseDto<T>(
    String status,
    String message,
    T data) {
  public static <T> ResponseDto<T> success(T data) {
    return success("OK", "Operation completed successfully", data);
  }

  public static <T> ResponseDto<T> success(String message, T data) {
    return success("OK", message, data);
  }

  public static <T> ResponseDto<T> success(String status, String message, T data) {
    return new ResponseDto<>(status, message, data);
  }

  public static <T> ResponseDto<T> error(String message) {
    return new ResponseDto<>("error", message, null);
  }

  public static <T> ResponseDto<T> fail(String message, T data) {
    return fail("fail", message, data);
  }

  public static <T> ResponseDto<T> fail(String status, String message, T data) {
    return new ResponseDto<>(status, message, data);
  }

  public static ResponseDto<Void> error(String status, String message) {
    return new ResponseDto<>(status, message, null);
  }
}
