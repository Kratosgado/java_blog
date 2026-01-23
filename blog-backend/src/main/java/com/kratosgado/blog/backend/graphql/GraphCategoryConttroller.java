package com.kratosgado.blog.backend.graphql;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import com.kratosgado.blog.models.Category;
import com.kratosgado.blog.backend.services.CategoryService;
import com.kratosgado.blog.dtos.request.CreateCategoryRequest;
import com.kratosgado.blog.dtos.request.UpdateCategoryRequest;
import com.kratosgado.blog.dtos.response.PageResponse;

@Controller
public class GraphCategoryConttroller {

  private final CategoryService categoryService;

  public GraphCategoryConttroller(CategoryService categoryService) {
    this.categoryService = categoryService;
  }

  @MutationMapping
  public boolean createCategory(@Argument CreateCategoryRequest input) {
    categoryService.createCategory(input);
    return true;
  }

  @QueryMapping
  public PageResponse<Category> categories(
      @Argument(name = "page") int page,
      @Argument(name = "size") int size) {
    return categoryService.getAllCategories(PageRequest.of(page, size));
  }

  @QueryMapping
  public Category category(@Argument Long id) {
    return categoryService.getCategoryById(id);
  }

  @MutationMapping
  public boolean deleteCategory(@Argument Long id) {
    categoryService.deleteCategory(id);
    return true;
  }

  @MutationMapping
  public Category updateCategory(@Argument Long id, @Argument UpdateCategoryRequest input) {
    CreateCategoryRequest request = new CreateCategoryRequest(input.name(), input.description());
    return categoryService.updateCategory(id, request);
  }

  // Field resolver for updatedAt
  @SchemaMapping(typeName = "Category", field = "updatedAt")
  public String updatedAt(Category category) {
    // Category model doesn't have updatedAt, so we'll return null
    // In a real application, you would add this field to the Category model
    return null;
  }
}
