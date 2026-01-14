package com.kratosgado.blog.utils.http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kratosgado.blog.dtos.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Base API client with common functionality
 */
public abstract class BaseApiClient {
  protected final Logger logger = LoggerFactory.getLogger(getClass());
  protected final HttpClient httpClient;
  protected final Gson gson;
  protected String authToken;

  protected BaseApiClient(String baseUrl) {
    this.httpClient = new HttpClient(baseUrl);
    this.gson = new Gson();
  }

  /**
   * Set authentication token for subsequent requests
   */
  public void setAuthToken(String token) {
    this.authToken = token;
  }

  /**
   * Parse ApiResponse from JSON with generic type
   */
  protected <T> ApiResponse<T> parseApiResponse(String json, Class<T> dataType) {
    Type type = TypeToken.getParameterized(ApiResponse.class, dataType).getType();
    return gson.fromJson(json, type);
  }

  /**
   * Parse ApiResponse from JSON with TypeToken
   */
  protected <T> ApiResponse<T> parseApiResponse(String json, Type type) {
    Type responseType = TypeToken.getParameterized(ApiResponse.class, type).getType();
    return gson.fromJson(json, responseType);
  }

  /**
   * Extract error message from response body
   */
  protected String extractErrorMessage(String responseBody) {
    try {
      Type type = new TypeToken<ApiResponse<Object>>(){}.getType();
      ApiResponse<Object> errorResponse = gson.fromJson(responseBody, type);
      if (errorResponse != null && errorResponse.message() != null) {
        return errorResponse.message();
      }
    } catch (Exception e) {
      logger.debug("Could not parse error response", e);
    }
    return responseBody != null ? responseBody : "Unknown error";
  }

  /**
   * Handle response and throw exception if not successful
   */
  protected <T> T handleResponse(HttpClient.HttpResponse<String> response, Class<T> dataType, String operation) throws IOException {
    if (!response.isSuccessful()) {
      String errorMessage = extractErrorMessage(response.getRawBody());
      logger.error("{} failed: {}", operation, errorMessage);
      throw new ApiException(operation + " failed: " + errorMessage, response.getStatusCode());
    }

    ApiResponse<T> apiResponse = parseApiResponse(response.getRawBody(), dataType);
    
    if (!"success".equals(apiResponse.status())) {
      throw new ApiException(apiResponse.message(), response.getStatusCode());
    }

    return apiResponse.data();
  }

  /**
   * Exception thrown when API call fails
   */
  public static class ApiException extends RuntimeException {
    private final int statusCode;

    public ApiException(String message, int statusCode) {
      super(message);
      this.statusCode = statusCode;
    }

    public int getStatusCode() {
      return statusCode;
    }
  }
}
