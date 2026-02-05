package com.kratosgado.blog.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Generic wrapper for all API responses.
 * Provides standardized response format with status code, message, and optional data/errors.
 * Used across all REST endpoints to ensure consistent response structure.
 *
 * @param <T> The type of data or error content
 */
@Schema(description = "Generic API response wrapper with status, message, and optional data/errors")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResponseDto<T>(
    @Schema(description = "HTTP status code", example = "200")
    int status,

    @Schema(description = "Human-readable response message", example = "Operation completed successfully")
    String message,

    @Schema(description = "Response data (present on success)")
    T data,

    @Schema(description = "Error details (present on failure)")
    T errors) {

  /**
   * Creates a successful response with default status 200 and message.
   *
   * @param data Response data
   * @param <T> Type of response data
   * @return ResponseDto with success status
   */
  public static <T> ResponseDto<T> success(T data) {
    return success(200, "Operation completed successfully", data);
  }

  /**
   * Creates a successful response with custom message.
   *
   * @param message Custom success message
   * @param data Response data
   * @param <T> Type of response data
   * @return ResponseDto with success status
   */
  public static <T> ResponseDto<T> success(String message, T data) {
    return success(200, message, data);
  }

  /**
   * Creates a successful response with custom status and message.
   *
   * @param status HTTP status code
   * @param message Custom success message
   * @param data Response data
   * @param <T> Type of response data
   * @return ResponseDto with specified status
   */
  public static <T> ResponseDto<T> success(int status, String message, T data) {
    return new ResponseDto<>(status, message, data, null);
  }

  /**
   * Creates an error response with default status 500.
   *
   * @param message Error message
   * @param <T> Type of error data
   * @return ResponseDto with error status
   */
  public static <T> ResponseDto<T> error(String message) {
    return new ResponseDto<>(500, message, null, null);
  }

  /**
   * Creates an error response with custom status.
   *
   * @param status HTTP status code
   * @param message Error message
   * @param <T> Type of error data
   * @return ResponseDto with error status
   */
  public static <T> ResponseDto<T> error(int status, String message) {
    return new ResponseDto<>(status, message, null, null);
  }

  /**
   * Creates a validation failure response with status 400.
   *
   * @param message Validation error message
   * @param data Validation error details
   * @param <T> Type of error data
   * @return ResponseDto with 400 status
   */
  public static <T> ResponseDto<T> fail(String message, T data) {
    return error(400, message, data);
  }

  /**
   * Creates an error response with custom status and error data.
   *
   * @param status HTTP status code
   * @param message Error message
   * @param data Error details
   * @param <T> Type of error data
   * @return ResponseDto with error status and details
   */
  public static <T> ResponseDto<T> error(int status, String message, T data) {
    return new ResponseDto<>(status, message, null, data);
  }
}
