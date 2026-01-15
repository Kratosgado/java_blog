package com.kratosgado.blog.utils.http;

import com.kratosgado.blog.dtos.request.LoginRequest;
import com.kratosgado.blog.dtos.request.RegisterRequest;
import com.kratosgado.blog.dtos.response.ResponseDto;
import com.kratosgado.blog.dtos.response.AuthResponse;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Client for authentication REST API endpoints
 */
public class AuthApiClient extends BaseApiClient {

  public AuthApiClient(String baseUrl) {
    super(baseUrl);
  }

  /**
   * Register a new user
   */
  public AuthResponse register(RegisterRequest request) throws IOException {
    logger.info("Registering user: {}", request.email());

    HttpClient.HttpResponse<String> response = httpClient.post("/auth/register", request, String.class);
    AuthResponse authResponse = handleResponse(response, AuthResponse.class, "Registration");

    logger.info("Registration successful for: {}", request.email());
    return authResponse;
  }

  /**
   * Login with email and password
   */
  public AuthResponse login(LoginRequest request) throws IOException {
    logger.info("Logging in user: {}", request.email());

    HttpClient.HttpResponse<String> response = httpClient.post("/auth/login", request, String.class);
    AuthResponse authResponse = handleResponse(response, AuthResponse.class, "Login");

    logger.info("Login successful for: {}", request.email());
    return authResponse;
  }

  /**
   * Validate a JWT token
   */
  public boolean validateToken(String token) {
    try {
      logger.debug("Validating token");

      HttpClient.HttpResponse<String> response = httpClient.get("/auth/validate", token, String.class);

      if (!response.isSuccessful()) {
        return false;
      }

      // Parse the response to check if token is valid
      Type type = new TypeToken<ResponseDto<java.util.Map<String, Object>>>() {
      }.getType();
      ResponseDto<java.util.Map<String, Object>> apiResponse = gson.fromJson(response.getRawBody(), type);

      if (apiResponse.data() != null && apiResponse.data().containsKey("valid")) {
        Boolean valid = (Boolean) apiResponse.data().get("valid");
        return valid != null && valid;
      }

      return false;
    } catch (IOException e) {
      logger.error("Token validation failed", e);
      return false;
    }
  }
}
