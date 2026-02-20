package com.kratosgado.blog.backend.controllers.v1;

import com.kratosgado.blog.backend.annotations.OpenApi.DeleteEndpoint;
import com.kratosgado.blog.backend.annotations.OpenApi.GetEndpoint;
import com.kratosgado.blog.backend.annotations.OpenApi.SecuredCreateEndpoint;
import com.kratosgado.blog.backend.annotations.OpenApi.UpdateEndpoint;
import com.kratosgado.blog.backend.models.Category;
import com.kratosgado.blog.backend.services.CategoryService;
import com.kratosgado.blog.dtos.request.CreateCategoryRequest;
import com.kratosgado.blog.dtos.request.PageRequest;
import com.kratosgado.blog.dtos.response.CategoryResponse;
import com.kratosgado.blog.dtos.response.PageResponse;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(
    name = "Categories",
    description = "Category management APIs")
public class CategoryController {
  private final CategoryService categoryService;

  @SecuredCreateEndpoint(
      summary = "Create a new category",
      description = "Creates a new category. Requires authentication.")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Category createCategory(
      @Valid @RequestBody @Parameter(description = "Category creation request")
          CreateCategoryRequest request) {
    return categoryService.createCategory(request);
  }

  @UpdateEndpoint(
      summary = "Update a category",
      description = "Updates an existing category. Requires authentication.")
  @PutMapping("/{id}")
  public Category updateCategory(
      @PathVariable("id") @Parameter(description = "Category ID") Long id,
      @Valid @RequestBody @Parameter(description = "Category update request")
          CreateCategoryRequest request) {
    return categoryService.updateCategory(id, request);
  }

  @DeleteEndpoint(
      summary = "Delete a category",
      description = "Deletes a category by ID. Requires authentication.")
  @DeleteMapping("/{id}")
  public void deleteCategory(
      @PathVariable("id") @Parameter(description = "Category ID") Long id) {
    categoryService.deleteCategory(id);
  }

  @GetMapping("/{id}")
  @GetEndpoint(
      summary = "Get a category by ID",
      description = "Retrieves a single category by its ID. Public access.")
  public Category getCategory(
      @PathVariable("id") @Parameter(description = "Category ID") Long id) {
    return categoryService.getCategoryById(id);
  }

  @GetMapping("/slug/{slug}")
  @GetEndpoint(
      summary = "Get a category by slug",
      description = "Retrieves a single category by its slug. Public access.")
  public Category getCategoryBySlug(
      @PathVariable @Parameter(description = "Category slug") String slug) {
    return categoryService.getCategoryBySlug(slug);
  }

  @GetMapping
  @GetEndpoint(
      summary = "Get all categories",
      description = "Retrieves a paginated list of all categories. Public access.")
  public PageResponse<Category> getAllCategories(@ParameterObject PageRequest page) {
    return categoryService.getAllCategories(page);
  }

  @GetMapping("/with-post-count")
  @GetEndpoint(
      summary = "Get all categories with post counts",
      description =
          "Retrieves a list of all categories including the number of posts in each. Public"
              + " access.")
  public PageResponse<CategoryResponse> getCategoriesWithPostCount() {
    List<CategoryResponse> categories = categoryService.getAllCategoriesWithPostCount();
    return new PageResponse<>(categories, 0, categories.size(), categories.size(), 1, true, true);
  }
}
