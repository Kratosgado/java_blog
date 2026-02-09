package com.kratosgado.blog.services;

import java.io.IOException;
import java.util.List;

import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.config.ApiConfig;
import com.kratosgado.blog.dtos.request.CreateTagRequest;
import com.kratosgado.blog.dtos.request.UpdateTagRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.models.Tag;
import com.kratosgado.blog.utils.context.AuthContext;
import com.kratosgado.blog.utils.http.BaseApiClient.ApiException;
import com.kratosgado.blog.utils.http.TagApiClient;

public class TagService {
  private static final Logger logger = LoggerFactory.getLogger(TagService.class);
  private final TagApiClient tagApiClient;

  @Inject
  public TagService() {
    this.tagApiClient = new TagApiClient(ApiConfig.getBaseUrl());
    // Set auth token from AuthContext
    String token = AuthContext.getInstance().getAuthToken();
    if (token != null) {
      this.tagApiClient.setAuthToken(token);
    }
  }

  private void ensureAuthToken() {
    String token = AuthContext.getInstance().getAuthToken();
    if (token != null) {
      this.tagApiClient.setAuthToken(token);
    }
  }

  public Tag getTagById(Long id) {
    ensureAuthToken();
    try {
      return tagApiClient.getTagById(id);
    } catch (IOException e) {
      logger.error("Failed to get tag due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get tag: {}", e.getMessage());
      throw new RuntimeException("Failed to get tag: " + e.getMessage(), e);
    }
  }

  public Tag getTagBySlug(String slug) {
    ensureAuthToken();
    try {
      return tagApiClient.getTagBySlug(slug);
    } catch (IOException e) {
      logger.error("Failed to get tag by slug due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get tag by slug: {}", e.getMessage());
      throw new RuntimeException("Failed to get tag by slug: " + e.getMessage(), e);
    }
  }

  public PageResponse<Tag> getAllTags(int page, int size) {
    ensureAuthToken();
    try {
      return tagApiClient.getAllTags(page, size);
    } catch (IOException e) {
      logger.error("Failed to get tags due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get tags: {}", e.getMessage());
      throw new RuntimeException("Failed to get tags: " + e.getMessage(), e);
    }
  }

  public PageResponse<Tag> searchTags(String keyword, int page, int size) {
    ensureAuthToken();
    try {
      return tagApiClient.searchTags(keyword, page, size);
    } catch (IOException e) {
      logger.error("Failed to search tags due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to search tags: {}", e.getMessage());
      throw new RuntimeException("Failed to search tags: " + e.getMessage(), e);
    }
  }

  public Tag createTag(CreateTagRequest request) {
    ensureAuthToken();
    try {
      return tagApiClient.createTag(request);
    } catch (IOException e) {
      logger.error("Failed to create tag due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to create tag: {}", e.getMessage());
      throw new RuntimeException("Failed to create tag: " + e.getMessage(), e);
    }
  }

  public Tag updateTag(Long id, UpdateTagRequest request) {
    ensureAuthToken();
    try {
      return tagApiClient.updateTag(id, request);
    } catch (IOException e) {
      logger.error("Failed to update tag due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to update tag: {}", e.getMessage());
      throw new RuntimeException("Failed to update tag: " + e.getMessage(), e);
    }
  }

  public void deleteTag(Long id) {
    ensureAuthToken();
    try {
      tagApiClient.deleteTag(id);
    } catch (IOException e) {
      logger.error("Failed to delete tag due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to delete tag: {}", e.getMessage());
      throw new RuntimeException("Failed to delete tag: " + e.getMessage(), e);
    }
  }

  // Convenience methods for controllers that expect List<Tag> instead of PageResponse

  /**
   * Get all tags as a list (for controllers)
   */
  public List<Tag> getAllTags() {
    ensureAuthToken();
    try {
      PageResponse<Tag> response = tagApiClient.getAllTags(0, 1000);
      return response.content();
    } catch (IOException e) {
      logger.error("Failed to get all tags due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get all tags: {}", e.getMessage());
      throw new RuntimeException("Failed to get all tags: " + e.getMessage(), e);
    }
  }

  /**
   * Get tags by post ID
   */
  public List<Tag> getTagsByPostId(Long postId) {
    ensureAuthToken();
    try {
      com.kratosgado.blog.utils.http.PostApiClient postApiClient = new com.kratosgado.blog.utils.http.PostApiClient(ApiConfig.getBaseUrl());
      postApiClient.setAuthToken(AuthContext.getInstance().getAuthToken());
      com.kratosgado.blog.dtos.response.PostResponse.PostDetails post = postApiClient.getPostById(postId);

      // PostDetails extends WithTag, so we can directly get tags
      var tags = post.getTags();
      if (tags == null) return List.of();
      return tags.stream()
          .map(ts -> new Tag(ts.getName(), ts.getSlug(), ""))
          .toList();
    } catch (Exception e) {
      logger.error("Failed to get tags for post {}: {}", postId, e.getMessage());
      return List.of();
    }
  }

  /**
   * Add tag to post (stub - not yet implemented in backend)
   */
  public void addTagToPost(Long postId, Long tagId) {
    logger.warn("addTagToPost() not yet implemented via API");
    throw new UnsupportedOperationException("addTagToPost not yet implemented via API");
  }

  /**
   * Remove tag from post (stub - not yet implemented in backend)
   */
  public void removeTagFromPost(Long postId, Long tagId) {
    logger.warn("removeTagFromPost() not yet implemented via API");
    throw new UnsupportedOperationException("removeTagFromPost not yet implemented via API");
  }
}
