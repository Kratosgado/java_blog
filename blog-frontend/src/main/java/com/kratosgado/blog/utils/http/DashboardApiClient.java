package com.kratosgado.blog.utils.http;

import com.google.gson.reflect.TypeToken;
import com.kratosgado.blog.dtos.response.ResponseDto;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Client for dashboard and analytics REST API endpoints
 */
public class DashboardApiClient extends BaseApiClient {

  public DashboardApiClient(String baseUrl) {
    super(baseUrl);
  }

  /**
   * Get overall dashboard statistics
   */
  public Map<String, Object> getDashboardStats() throws IOException {
    logger.info("Fetching dashboard statistics");

    HttpClient.HttpResponse<String> response = httpClient.get("/dashboard/stats", authToken, String.class);

    return handleMapResponse(response, "Get dashboard stats");
  }

  /**
   * Get user-specific dashboard statistics
   */
  public Map<String, Object> getUserDashboardStats() throws IOException {
    logger.info("Fetching user dashboard statistics");

    HttpClient.HttpResponse<String> response = httpClient.get("/dashboard/user/stats", authToken, String.class);

    return handleMapResponse(response, "Get user dashboard stats");
  }

  /**
   * Get analytics data for a date range
   */
  public Map<String, Object> getAnalytics(LocalDateTime startDate, LocalDateTime endDate) throws IOException {
    logger.info("Fetching analytics data from {} to {}", startDate, endDate);

    DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
    String endpoint = String.format("/dashboard/analytics?startDate=%s&endDate=%s",
        startDate.format(formatter),
        endDate.format(formatter));

    HttpClient.HttpResponse<String> response = httpClient.get(endpoint, authToken, String.class);

    return handleMapResponse(response, "Get analytics");
  }

  /**
   * Get post status distribution
   */
  public Map<String, Long> getPostStatusDistribution() throws IOException {
    logger.info("Fetching post status distribution");

    HttpClient.HttpResponse<String> response = httpClient.get("/dashboard/posts/distribution", authToken,
        String.class);

    if (!response.isSuccessful()) {
      String errorMessage = extractErrorMessage(response.getRawBody());
      logger.error("Get post distribution failed: {}", errorMessage);
      throw new ApiException("Get post distribution failed: " + errorMessage, response.getStatusCode());
    }

    Type responseType = new TypeToken<ResponseDto<Map<String, Long>>>() {
    }.getType();
    ResponseDto<Map<String, Long>> apiResponse = gson.fromJson(response.getRawBody(), responseType);

    if (!"success".equals(apiResponse.status())) {
      throw new ApiException(apiResponse.message(), response.getStatusCode());
    }

    return apiResponse.data();
  }

  /**
   * Helper method to handle Map response types
   */
  private Map<String, Object> handleMapResponse(HttpClient.HttpResponse<String> response, String operation)
      throws IOException {
    if (!response.isSuccessful()) {
      String errorMessage = extractErrorMessage(response.getRawBody());
      logger.error("{} failed: {}", operation, errorMessage);
      throw new ApiException(operation + " failed: " + errorMessage, response.getStatusCode());
    }

    Type responseType = new TypeToken<ResponseDto<Map<String, Object>>>() {
    }.getType();
    ResponseDto<Map<String, Object>> apiResponse = gson.fromJson(response.getRawBody(), responseType);

    if (!"success".equals(apiResponse.status())) {
      throw new ApiException(apiResponse.message(), response.getStatusCode());
    }

    return apiResponse.data();
  }
}
