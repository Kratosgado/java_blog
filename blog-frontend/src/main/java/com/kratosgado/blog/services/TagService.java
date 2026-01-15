package com.kratosgado.blog.services;

import java.io.IOException;
import java.util.List;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.config.ApiConfig;
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

  public List<Tag> getAllTags() {
    ensureAuthToken();
    try {
      return tagApiClient.getAllTags();
    } catch (IOException e) {
      logger.error("Failed to get tags due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get tags: {}", e.getMessage());
      throw new RuntimeException("Failed to get tags: " + e.getMessage(), e);
    }
  }

  // Stub methods for backward compatibility - to be implemented when needed
  public List<com.kratosgado.blog.models.Tag> getTagsByPostId(Long postId) {
    logger.warn("getTagsByPostId() not yet implemented via API");
    throw new UnsupportedOperationException("getTagsByPostId not yet implemented via API");
  }

  public boolean addTagToPost(Long postId, Long tagId) {
    logger.warn("addTagToPost() not yet implemented via API");
    throw new UnsupportedOperationException("addTagToPost not yet implemented via API");
  }

  public boolean removeTagFromPost(Long postId, Long tagId) {
    logger.warn("removeTagFromPost() not yet implemented via API");
    throw new UnsupportedOperationException("removeTagFromPost not yet implemented via API");
  }

  public void deleteTag(Long id) {
    logger.warn("deleteTag() not yet implemented via API");
    throw new UnsupportedOperationException("deleteTag not yet implemented via API");
  }
}
