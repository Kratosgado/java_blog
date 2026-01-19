package com.kratosgado.blog.utils.http;

import com.google.gson.Gson;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * HTTP client utility using OkHttp for making REST API calls
 */
public class HttpClient {
  private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);
  private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

  private final OkHttpClient client;
  private final Gson gson;
  private final String baseUrl;

  public HttpClient(String baseUrl) {
    this.baseUrl = baseUrl;
    this.gson = GsonFactory.getGson();
    this.client = new OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build();
  }

  /**
   * Make a GET request
   */
  public <T> HttpResponse<T> get(String endpoint, Class<T> responseType) throws IOException {
    return get(endpoint, null, responseType);
  }

  /**
   * Make a GET request with authorization token
   */
  public <T> HttpResponse<T> get(String endpoint, String token, Class<T> responseType) throws IOException {
    Request.Builder requestBuilder = new Request.Builder()
        .url(baseUrl + endpoint)
        .get();

    if (token != null && !token.isEmpty()) {
      requestBuilder.header("Authorization", "Bearer " + token);
    }

    return executeRequest(requestBuilder.build(), responseType);
  }

  /**
   * Make a POST request with JSON body
   */
  public <T> HttpResponse<T> post(String endpoint, Object body, Class<T> responseType) throws IOException {
    return post(endpoint, body, null, responseType);
  }

  /**
   * Make a POST request with JSON body and authorization token
   */
  public <T> HttpResponse<T> post(String endpoint, Object body, String token, Class<T> responseType)
      throws IOException {
    String json = gson.toJson(body);
    RequestBody requestBody = RequestBody.create(json, JSON);

    Request.Builder requestBuilder = new Request.Builder()
        .url(baseUrl + endpoint)
        .post(requestBody);

    if (token != null && !token.isEmpty()) {
      requestBuilder.header("Authorization", "Bearer " + token);
    }

    return executeRequest(requestBuilder.build(), responseType);
  }

  /**
   * Make a PUT request with JSON body
   */
  public <T> HttpResponse<T> put(String endpoint, Object body, String token, Class<T> responseType) throws IOException {
    String json = gson.toJson(body);
    RequestBody requestBody = RequestBody.create(json, JSON);

    Request.Builder requestBuilder = new Request.Builder()
        .url(baseUrl + endpoint)
        .put(requestBody);

    if (token != null && !token.isEmpty()) {
      requestBuilder.header("Authorization", "Bearer " + token);
    }

    return executeRequest(requestBuilder.build(), responseType);
  }

  /**
   * Make a DELETE request
   */
  public <T> HttpResponse<T> delete(String endpoint, String token, Class<T> responseType) throws IOException {
    Request.Builder requestBuilder = new Request.Builder()
        .url(baseUrl + endpoint)
        .delete();

    if (token != null && !token.isEmpty()) {
      requestBuilder.header("Authorization", "Bearer " + token);
    }

    return executeRequest(requestBuilder.build(), responseType);
  }

  /**
   * Execute the request and parse response
   */
  private <T> HttpResponse<T> executeRequest(Request request, Class<T> responseType) throws IOException {
    logger.debug("Making request: {} {}", request.method(), request.url());

    try (Response response = client.newCall(request).execute()) {
      String responseBody = response.body() != null ? response.body().string() : null;

      logger.debug("Response status: {}", response.code());
      logger.debug("Response body: {}", responseBody);

      T data = null;
      if (responseBody != null && !responseBody.isEmpty() && responseType != Void.class) {
        // For String.class, return the raw body instead of trying to parse it
        if (responseType == String.class) {
          data = responseType.cast(responseBody);
        } else {
          data = gson.fromJson(responseBody, responseType);
        }
      }

      return new HttpResponse<>(
          response.code(),
          response.isSuccessful(),
          data,
          responseBody);
    }
  }

  /**
   * Response wrapper containing status code, success flag, and parsed data
   */
  public static class HttpResponse<T> {
    private final int statusCode;
    private final boolean successful;
    private final T data;
    private final String rawBody;

    public HttpResponse(int statusCode, boolean successful, T data, String rawBody) {
      this.statusCode = statusCode;
      this.successful = successful;
      this.data = data;
      this.rawBody = rawBody;
    }

    public int getStatusCode() {
      return statusCode;
    }

    public boolean isSuccessful() {
      return successful;
    }

    public T getData() {
      return data;
    }

    public String getRawBody() {
      return rawBody;
    }
  }
}
