package com.kratosgado.blog.config;

/**
 * Configuration for API endpoints
 */
public class ApiConfig {
  private static final String DEFAULT_BASE_URL = "http://localhost:8080/api";
  
  private static String baseUrl = DEFAULT_BASE_URL;
  
  public static String getBaseUrl() {
    // Check system property first
    String systemBaseUrl = System.getProperty("api.baseUrl");
    if (systemBaseUrl != null && !systemBaseUrl.isEmpty()) {
      return systemBaseUrl;
    }
    
    // Check environment variable
    String envBaseUrl = System.getenv("API_BASE_URL");
    if (envBaseUrl != null && !envBaseUrl.isEmpty()) {
      return envBaseUrl;
    }
    
    return baseUrl;
  }
  
  public static void setBaseUrl(String url) {
    baseUrl = url;
  }
  
  public static String getDefaultBaseUrl() {
    return DEFAULT_BASE_URL;
  }
}
