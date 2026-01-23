package com.kratosgado.blog.utils.http;

import com.google.gson.reflect.TypeToken;
import com.kratosgado.blog.dtos.request.CreateTagRequest;
import com.kratosgado.blog.dtos.request.UpdateTagRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.models.Tag;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Client for tag REST API endpoints
 */
public class TagApiClient extends BaseApiClient {

  public TagApiClient(String baseUrl) {
    super(baseUrl);
  }

  /**
   * Get all tags with pagination
   */
  public PageResponse<Tag> getAllTags(int page, int size) throws IOException {
    logger.info("Fetching all tags - page: {}, size: {}", page, size);

    String endpoint = String.format("/tags?page=%d&size=%d", page, size);
    HttpClient.HttpResponse<String> response = httpClient.get(endpoint, authToken, String.class);

    Type pageType = new TypeToken<PageResponse<Tag>>() {
    }.getType();
    return gson.fromJson(response.getRawBody(), pageType);
  }

  /**
   * Get tag by ID
   */
  public Tag getTagById(Long id) throws IOException {
    logger.info("Fetching tag by ID: {}", id);

    HttpClient.HttpResponse<String> response = httpClient.get("/tags/" + id, authToken, String.class);

    return handleResponse(response, Tag.class, "Get tag by ID");
  }

  /**
   * Get tag by slug
   */
  public Tag getTagBySlug(String slug) throws IOException {
    logger.info("Fetching tag by slug: {}", slug);

    HttpClient.HttpResponse<String> response = httpClient.get("/tags/slug/" + slug, authToken, String.class);

    return handleResponse(response, Tag.class, "Get tag by slug");
  }

  /**
   * Search tags by keyword with pagination
   */
  public PageResponse<Tag> searchTags(String keyword, int page, int size) throws IOException {
    logger.info("Searching tags with keyword: {}", keyword);

    String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
    String endpoint = String.format("/tags/search?keyword=%s&page=%d&size=%d", encodedKeyword, page, size);
    HttpClient.HttpResponse<String> response = httpClient.get(endpoint, authToken, String.class);

    Type pageType = new TypeToken<PageResponse<Tag>>() {
    }.getType();
    return gson.fromJson(response.getRawBody(), pageType);
  }

  /**
   * Create a new tag
   */
  public Tag createTag(CreateTagRequest request) throws IOException {
    logger.info("Creating new tag: {}", request.name());

    HttpClient.HttpResponse<String> response = httpClient.post("/tags", request, authToken, String.class);

    Tag tag = handleResponse(response, Tag.class, "Create tag");
    logger.info("Tag created successfully with ID: {}", tag.getId());
    return tag;
  }

  /**
   * Update an existing tag
   */
  public Tag updateTag(Long id, UpdateTagRequest request) throws IOException {
    logger.info("Updating tag ID: {}", id);

    HttpClient.HttpResponse<String> response = httpClient.put("/tags/" + id, request, authToken, String.class);

    Tag tag = handleResponse(response, Tag.class, "Update tag");
    logger.info("Tag updated successfully");
    return tag;
  }

  /**
   * Delete a tag
   */
  public void deleteTag(Long id) throws IOException {
    logger.info("Deleting tag ID: {}", id);

    HttpClient.HttpResponse<String> response = httpClient.delete("/tags/" + id, authToken, String.class);

    if (!response.isSuccessful()) {
      String errorMessage = extractErrorMessage(response.getRawBody());
      logger.error("Delete tag failed: {}", errorMessage);
      throw new ApiException("Delete tag failed: " + errorMessage, response.getStatusCode());
    }

    logger.info("Tag deleted successfully");
  }
}
