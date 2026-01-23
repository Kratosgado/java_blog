package com.kratosgado.blog.services;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.kratosgado.blog.config.ApiConfig;
import com.kratosgado.blog.utils.context.AuthContext;
import com.kratosgado.blog.utils.http.BaseApiClient.ApiException;
import com.kratosgado.blog.utils.http.DashboardApiClient;

/**
 * Service for handling dashboard and analytics operations via REST API
 */
public class DashboardService {
  private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);
  private final DashboardApiClient dashboardApiClient;

  @Inject
  public DashboardService() {
    this.dashboardApiClient = new DashboardApiClient(ApiConfig.getBaseUrl());
    // Set auth token from AuthContext
    String token = AuthContext.getInstance().getAuthToken();
    if (token != null) {
      this.dashboardApiClient.setAuthToken(token);
    }
    logger.info("DashboardService initialized with API backend");
  }

  private void ensureAuthToken() {
    String token = AuthContext.getInstance().getAuthToken();
    if (token != null) {
      this.dashboardApiClient.setAuthToken(token);
    }
  }

  public Map<String, Object> getDashboardStats() {
    ensureAuthToken();
    try {
      return dashboardApiClient.getDashboardStats();
    } catch (IOException e) {
      logger.error("Failed to get dashboard stats due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get dashboard stats: {}", e.getMessage());
      throw new RuntimeException("Failed to get dashboard stats: " + e.getMessage(), e);
    }
  }

  public Map<String, Object> getUserDashboardStats() {
    ensureAuthToken();
    try {
      return dashboardApiClient.getUserDashboardStats();
    } catch (IOException e) {
      logger.error("Failed to get user dashboard stats due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get user dashboard stats: {}", e.getMessage());
      throw new RuntimeException("Failed to get user dashboard stats: " + e.getMessage(), e);
    }
  }

  public Map<String, Object> getAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
    ensureAuthToken();
    try {
      return dashboardApiClient.getAnalytics(startDate, endDate);
    } catch (IOException e) {
      logger.error("Failed to get analytics due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get analytics: {}", e.getMessage());
      throw new RuntimeException("Failed to get analytics: " + e.getMessage(), e);
    }
  }

  public Map<String, Long> getPostStatusDistribution() {
    ensureAuthToken();
    try {
      return dashboardApiClient.getPostStatusDistribution();
    } catch (IOException e) {
      logger.error("Failed to get post distribution due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get post distribution: {}", e.getMessage());
      throw new RuntimeException("Failed to get post distribution: " + e.getMessage(), e);
    }
  }
}
