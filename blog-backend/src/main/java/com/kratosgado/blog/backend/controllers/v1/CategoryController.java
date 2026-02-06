package com.kratosgado.blog.backend.controllers.v1;

import com.kratosgado.blog.backend.annotations.OpenApi.DeleteEndpoint;
import com.kratosgado.blog.backend.annotations.OpenApi.GetEndpoint;
import com.kratosgado.blog.backend.annotations.OpenApi.SecuredCreateEndpoint;
import com.kratosgado.blog.backend.annotations.OpenApi.UpdateEndpoint;
import com.kratosgado.blog.backend.services.CategoryService;
import com.kratosgado.blog.dtos.request.CreateCategoryRequest;
import com.kratosgado.blog.dtos.request.PageRequest;
import com.kratosgado.blog.dtos.response.CategoryResponse;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.models.Category;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
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
@io.swagger.v3.oas.annotations.tags.Tag(
    name = "Categories",
    description = "Category management APIs")
public class CategoryController {
  private final CategoryService categoryService;

  public CategoryController(CategoryService categoryService) {
    this.categoryService = categoryService;
  }

  @SecuredCreateEndpoint
  @PostMapping
  @Operation(
      summary = "Create a new category",
      description = "Creates a new category. Requires authentication.",
      security = @SecurityRequirement(name = "bearer-jwt"))
  @ResponseStatus(HttpStatus.CREATED)
  public Category createCategory(
      @Valid @RequestBody @Parameter(description = "Category creation request")
          CreateCategoryRequest request) {
    return categoryService.createCategory(request);
  }

  @UpdateEndpoint
  @PutMapping("/{id}")
  @Operation(
      summary = "Update a category",
      description = "Updates an existing category. Requires authentication.",
      security = @SecurityRequirement(name = "bearer-jwt"))
  public Category updateCategory(
      @PathVariable("id") @Parameter(description = "Category ID") Long id,
      @Valid @RequestBody @Parameter(description = "Category update request")
          CreateCategoryRequest request) {
    return categoryService.updateCategory(id, request);
  }

  @DeleteMapping("/{id}")
  @DeleteEndpoint
  @Operation(
      summary = "Delete a category",
      description = "Deletes a category by ID. Requires authentication.",
      security = @SecurityRequirement(name = "bearer-jwt"))
  public void deleteCategory(@PathVariable("id") @Parameter(description = "Category ID") Long id) {
    categoryService.deleteCategory(id);
  }

  @GetMapping("/{id}")
  @GetEndpoint
  @Operation(
      summary = "Get a category by ID",
      description = "Retrieves a single category by its ID. Public access.")
  public Category getCategory(@PathVariable("id") @Parameter(description = "Category ID") Long id) {
    return categoryService.getCategoryById(id);
  }

  @GetMapping("/slug/{slug}")
  @GetEndpoint
  @Operation(
      summary = "Get a category by slug",
      description = "Retrieves a single category by its slug. Public access.")
  public Category getCategoryBySlug(
      @PathVariable @Parameter(description = "Category slug") String slug) {
    return categoryService.getCategoryBySlug(slug);
  }

  @GetMapping
  @GetEndpoint
  @Operation(
      summary = "Get all categories",
      description = "Retrieves a paginated list of all categories. Public access.")
  public PageResponse<Category> getAllCategories(@ParameterObject PageRequest page) {
    return categoryService.getAllCategories(page);
  }

  @GetMapping("/with-post-count")
  @GetEndpoint
  @Operation(
      summary = "Get all categories with post counts",
      description =
          "Retrieves a list of all categories including the number of posts in each. Public"
              + " access.")
  public List<CategoryResponse> getCategoriesWithPostCount() {
    return categoryService.getAllCategoriesWithPostCount();
  }
}
