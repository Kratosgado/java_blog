package com.kratosgado.blog.utils.http;

import com.kratosgado.blog.models.Tag;

import java.io.IOException;
import java.util.List;

/**
 * Client for tag REST API endpoints
 */
public class TagApiClient extends BaseApiClient {

  public TagApiClient(String baseUrl) {
    super(baseUrl);
  }

  /**
   * Get all tags
   */
  public List<Tag> getAllTags() throws IOException {
    logger.info("Fetching all tags");
    
    HttpClient.HttpResponse<String> response = httpClient.get("/tags", authToken, String.class);
    
    return handleResponse(response, List.class, "Get tags");
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
}
