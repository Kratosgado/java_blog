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
import com.kratosgado.blog.dtos.response.ResponseDto;
import com.kratosgado.blog.models.Category;
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
  public ResponseDto<Category> createCategory(
      @Valid @RequestBody @Parameter(description = "Category creation request")
          CreateCategoryRequest request) {
    Category category = categoryService.createCategory(request);
    return ResponseDto.success(
        HttpStatus.CREATED.value(), "Category created successfully", category);
  }

  @UpdateEndpoint(
      summary = "Update a category",
      description = "Updates an existing category. Requires authentication.")
  @PutMapping("/{id}")
  public ResponseDto<Category> updateCategory(
      @PathVariable("id") @Parameter(description = "Category ID") Long id,
      @Valid @RequestBody @Parameter(description = "Category update request")
          CreateCategoryRequest request) {
    Category category = categoryService.updateCategory(id, request);
    return ResponseDto.success("Category updated successfully", category);
  }

  @DeleteEndpoint(
      summary = "Delete a category",
      description = "Deletes a category by ID. Requires authentication.")
  @DeleteMapping("/{id}")
  public ResponseDto<Void> deleteCategory(
      @PathVariable("id") @Parameter(description = "Category ID") Long id) {
    categoryService.deleteCategory(id);
    return ResponseDto.success("Category deleted successfully", null);
  }

  @GetMapping("/{id}")
  @GetEndpoint(
      summary = "Get a category by ID",
      description = "Retrieves a single category by its ID. Public access.")
  public ResponseDto<Category> getCategory(
      @PathVariable("id") @Parameter(description = "Category ID") Long id) {
    Category category = categoryService.getCategoryById(id);
    return ResponseDto.success(category);
  }

  @GetMapping("/slug/{slug}")
  @GetEndpoint(
      summary = "Get a category by slug",
      description = "Retrieves a single category by its slug. Public access.")
  public ResponseDto<Category> getCategoryBySlug(
      @PathVariable @Parameter(description = "Category slug") String slug) {
    Category category = categoryService.getCategoryBySlug(slug);
    return ResponseDto.success(category);
  }

  @GetMapping
  @GetEndpoint(
      summary = "Get all categories",
      description = "Retrieves a paginated list of all categories. Public access.")
  public ResponseDto<PageResponse<Category>> getAllCategories(@ParameterObject PageRequest page) {
    PageResponse<Category> categories = categoryService.getAllCategories(page);
    return ResponseDto.success(categories);
  }

  @GetMapping("/with-post-count")
  @GetEndpoint(
      summary = "Get all categories with post counts",
      description =
          "Retrieves a list of all categories including the number of posts in each. Public"
              + " access.")
  public ResponseDto<PageResponse<CategoryResponse>> getCategoriesWithPostCount() {
    List<CategoryResponse> categories = categoryService.getAllCategoriesWithPostCount();
    PageResponse<CategoryResponse> pageResponse =
        new PageResponse<>(categories, 0, categories.size(), categories.size(), 1, true, true);
    return ResponseDto.success(pageResponse);
  }
}
