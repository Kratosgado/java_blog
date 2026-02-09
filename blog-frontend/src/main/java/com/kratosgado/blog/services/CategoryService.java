package com.kratosgado.blog.services;

import java.io.IOException;
import java.util.List;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.config.ApiConfig;
import com.kratosgado.blog.dtos.request.CreateCategoryRequest;
import com.kratosgado.blog.dtos.request.UpdateCategoryRequest;
import com.kratosgado.blog.dtos.response.CategoryResponse;
import com.kratosgado.blog.models.Category;
import com.kratosgado.blog.utils.context.AuthContext;
import com.kratosgado.blog.utils.http.BaseApiClient.ApiException;
import com.kratosgado.blog.utils.http.CategoryApiClient;

public class CategoryService {
  private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);
  private final CategoryApiClient categoryApiClient;

  @Inject
  public CategoryService() {
    this.categoryApiClient = new CategoryApiClient(ApiConfig.getBaseUrl());
    // Set auth token from AuthContext
    String token = AuthContext.getInstance().getAuthToken();
    if (token != null) {
      this.categoryApiClient.setAuthToken(token);
    }
  }

  private void ensureAuthToken() {
    String token = AuthContext.getInstance().getAuthToken();
    if (token != null) {
      this.categoryApiClient.setAuthToken(token);
    }
  }

  public Category createCategory(CreateCategoryRequest request) {
    ensureAuthToken();
    try {
      return categoryApiClient.createCategory(request);
    } catch (IOException e) {
      logger.error("Failed to create category due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to create category: {}", e.getMessage());
      throw new RuntimeException("Failed to create category: " + e.getMessage(), e);
    }
  }

  public Category updateCategory(Long id, UpdateCategoryRequest request) {
    ensureAuthToken();
    try {
      return categoryApiClient.updateCategory(id, request);
    } catch (IOException e) {
      logger.error("Failed to update category due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to update category: {}", e.getMessage());
      throw new RuntimeException("Failed to update category: " + e.getMessage(), e);
    }
  }

  public void deleteCategory(Long id) {
    ensureAuthToken();
    try {
      categoryApiClient.deleteCategory(id);
    } catch (IOException e) {
      logger.error("Failed to delete category due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to delete category: {}", e.getMessage());
      throw new RuntimeException("Failed to delete category: " + e.getMessage(), e);
    }
  }

  public Category getCategoryById(Long id) {
    ensureAuthToken();
    try {
      return categoryApiClient.getCategoryById(id);
    } catch (IOException e) {
      logger.error("Failed to get category due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get category: {}", e.getMessage());
      throw new RuntimeException("Failed to get category: " + e.getMessage(), e);
    }
  }

  public Category getCategoryBySlug(String slug) {
    ensureAuthToken();
    try {
      return categoryApiClient.getCategoryBySlug(slug);
    } catch (IOException e) {
      logger.error("Failed to get category by slug due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get category by slug: {}", e.getMessage());
      throw new RuntimeException("Failed to get category by slug: " + e.getMessage(), e);
    }
  }

  public List<Category> getAllCategories() {
    ensureAuthToken();
    try {
      return categoryApiClient.getAllCategories();
    } catch (IOException e) {
      logger.error("Failed to get categories due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get categories: {}", e.getMessage());
      throw new RuntimeException("Failed to get categories: " + e.getMessage(), e);
    }
  }

  // Stub methods for backward compatibility - to be implemented when needed
  public List<com.kratosgado.blog.models.Category> getCategoriesByPostId(Long postId) {
    logger.warn("getCategoriesByPostId() not yet implemented via API - returning empty list");
    // Return empty list for now - this would need a new backend endpoint
    return List.of();
  }

  public boolean addCategoryToPost(Long postId, Long categoryId) {
    logger.warn("addCategoryToPost() not yet implemented via API - returning false");
    // Return false for now - this would need a new backend endpoint
    return false;
  }

  public boolean removeCategoryFromPost(Long postId, Long categoryId) {
    logger.warn("removeCategoryFromPost() not yet implemented via API - returning false");
    // Return false for now - this would need a new backend endpoint
    return false;
  }

  public int getCategoryCount() {
    logger.debug("getCategoryCount() using getAllCategories().size() as workaround");
    return getAllCategories().size(); // Temporary workaround
  }

  /**
   * Get all categories with post count for UI display
   */
  public List<CategoryResponse> getAllCategoriesWithPostCount() {
    ensureAuthToken();
    try {
      return categoryApiClient.getAllCategoriesWithPostCount();
    } catch (IOException e) {
      logger.error("Failed to get categories with post count due to network error", e);
      throw new RuntimeException("Failed to connect to server: " + e.getMessage(), e);
    } catch (ApiException e) {
      logger.error("Failed to get categories with post count: {}", e.getMessage());
      throw new RuntimeException("Failed to get categories with post count: " + e.getMessage(), e);
    }
  }
}
