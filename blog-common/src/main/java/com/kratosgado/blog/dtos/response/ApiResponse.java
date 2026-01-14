package com.kratosgado.blog.dtos.response;

public record ApiResponse<T>(
    String status,
    String message,
    T data) {
  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>("success", "Operation completed successfully", data);
  }

  public static <T> ApiResponse<T> success(String message, T data) {
    return new ApiResponse<>("success", message, data);
  }

  public static <T> ApiResponse<T> error(String message) {
    return new ApiResponse<>("error", message, null);
  }

  public static <T> ApiResponse<T> fail(String message, T data) {
    return new ApiResponse<>("fail", message, data);
  }

  public static ApiResponse<Void> error(String status, String message) {
    return new ApiResponse<>(status, message, null);
  }
}
