package com.kratosgado.blog.utils.http;

import com.kratosgado.blog.dtos.request.CreateCategoryRequest;
import com.kratosgado.blog.dtos.request.UpdateCategoryRequest;
import com.kratosgado.blog.models.Category;

import java.io.IOException;
import java.util.List;

/**
 * Client for category REST API endpoints
 */
public class CategoryApiClient extends BaseApiClient {

  public CategoryApiClient(String baseUrl) {
    super(baseUrl);
  }

  /**
   * Get all categories
   */
  public List<Category> getAllCategories() throws IOException {
    logger.info("Fetching all categories");

    HttpClient.HttpResponse<String> response = httpClient.get("/categories", authToken, String.class);

    return handleResponse(response, List.class, "Get categories");
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

    Category category = handleResponse(response, Category.class, "Create category");
    logger.info("Category created successfully with ID: {}", category.getId());
    return category;
  }

  /**
   * Update an existing category
   */
  public Category updateCategory(Long id, UpdateCategoryRequest request) throws IOException {
    logger.info("Updating category ID: {}", id);

    HttpClient.HttpResponse<String> response = httpClient.put("/categories/" + id, request, authToken, String.class);

    Category category = handleResponse(response, Category.class, "Update category");
    logger.info("Category updated successfully");
    return category;
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
}
