package com.kratosgado.blog.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResponseDto<T>(
    int status,
    String message,
    T data, T errors) {
  public static <T> ResponseDto<T> success(T data) {
    return success(200, "Operation completed successfully", data);
  }

  public static <T> ResponseDto<T> success(String message, T data) {
    return success(200, message, data);
  }

  public static <T> ResponseDto<T> success(int status, String message, T data) {
    return new ResponseDto<>(status, message, data, null);
  }

  public static <T> ResponseDto<T> error(String message) {
    return new ResponseDto<>(500, message, null, null);
  }

  public static <T> ResponseDto<T> error(int status, String message) {
    return new ResponseDto<>(status, message, null, null);
  }

  public static <T> ResponseDto<T> fail(String message, T data) {
    return error(400, message, data);
  }

  public static <T> ResponseDto<T> error(int status, String message, T data) {
    return new ResponseDto<>(status, message, null, data);
  }
}
