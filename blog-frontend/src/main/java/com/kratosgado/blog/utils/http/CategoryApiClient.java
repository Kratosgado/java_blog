package com.kratosgado.blog.utils.http;

import com.kratosgado.blog.dtos.request.CreateCategoryRequest;
import com.kratosgado.blog.dtos.request.UpdateCategoryRequest;
import com.kratosgado.blog.dtos.response.CategoryResponse;
import com.kratosgado.blog.models.Category;

import com.google.gson.reflect.TypeToken;
import com.kratosgado.blog.dtos.response.PageResponse;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Client for category REST API endpoints
 */
public class CategoryApiClient extends BaseApiClient {

  public CategoryApiClient(String baseUrl) {
    super(baseUrl);
  }

  /**
   * Get all categories with pagination
   */
  public PageResponse<Category> getAllCategories(int page, int size) throws IOException {
    logger.info("Fetching categories - page: {}, size: {}", page, size);

    String endpoint = String.format("/categories?page=%d&size=%d", page, size);
    HttpClient.HttpResponse<String> response = httpClient.get(endpoint, authToken, String.class);

    Type pageResponseType = TypeToken.getParameterized(PageResponse.class, Category.class).getType();
    return handleResponse(response, pageResponseType, "Get categories");
  }

  /**
   * Get all categories (convenience for old callers)
   */
  public List<Category> getAllCategories() throws IOException {
    return getAllCategories(0, 1000).content();
  }

  /**
   * Get category by ID
   */
  public Category getCategoryById(Long id) throws IOException {
    logger.info("Fetching category by ID: {}", id);

    HttpClient.HttpResponse<String> response = httpClient.get("/categories/" + id, authToken, String.class);

    return handleResponse(response, Category.class, "Get category by ID");
  }

  /**
   * Get category by slug
   */
  public Category getCategoryBySlug(String slug) throws IOException {
    logger.info("Fetching category by slug: {}", slug);

    HttpClient.HttpResponse<String> response = httpClient.get("/categories/slug/" + slug, authToken, String.class);

    return handleResponse(response, Category.class, "Get category by slug");
  }

  /**
   * Create a new category
   */
  public Category createCategory(CreateCategoryRequest request) throws IOException {
    logger.info("Creating new category: {}", request.name());

    HttpClient.HttpResponse<String> response = httpClient.post("/categories", request, authToken, String.class);

    return handleResponse(response, Category.class, "Create category");
  }

  /**
   * Update an existing category
   */
  public Category updateCategory(Long id, UpdateCategoryRequest request) throws IOException {
    logger.info("Updating category ID: {}", id);

    HttpClient.HttpResponse<String> response = httpClient.put("/categories/" + id, request, authToken, String.class);

    return handleResponse(response, Category.class, "Update category");
  }

  /**
   * Delete a category
   */
  public void deleteCategory(Long id) throws IOException {
    logger.info("Deleting category ID: {}", id);

    HttpClient.HttpResponse<String> response = httpClient.delete("/categories/" + id, authToken, String.class);

    if (!response.isSuccessful()) {
      String errorMessage = extractErrorMessage(response.getRawBody());
      logger.error("Delete category failed: {}", errorMessage);
      throw new ApiException("Delete category failed: " + errorMessage, response.getStatusCode());
    }

    logger.info("Category deleted successfully");
  }

  /**
   * Get all categories with post count
   */
  public List<CategoryResponse> getAllCategoriesWithPostCount() throws IOException {
    logger.info("Fetching categories with post count");

    HttpClient.HttpResponse<String> response = httpClient.get("/categories/with-post-count", authToken, String.class);

    Type listType = TypeToken.getParameterized(List.class, CategoryResponse.class).getType();
    return handleResponse(response, listType, "Get categories with post count");
  }
}
