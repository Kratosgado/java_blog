package com.kratosgado.blog.backend.graphql;

import com.kratosgado.blog.backend.services.CategoryService;
import com.kratosgado.blog.dtos.request.CreateCategoryRequest;
import com.kratosgado.blog.dtos.request.PageRequest;
import com.kratosgado.blog.dtos.request.UpdateCategoryRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.backend.models.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class GraphCategoryConttroller {

  private final CategoryService categoryService;

  @MutationMapping
  public Category createCategory(@Argument CreateCategoryRequest input) {
    return categoryService.createCategory(input);
  }

  @QueryMapping
  public PageResponse<Category> categories(
      @Argument(name = "page") int page, @Argument(name = "size") int size) {
    return categoryService.getAllCategories(new PageRequest(page, size, "id", "desc"));
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
    return null;
  }
}

