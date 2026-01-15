package com.kratosgado.blog.backend.graphql;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.kratosgado.blog.models.Category;
import com.kratosgado.blog.backend.services.CategoryService;
import com.kratosgado.blog.dtos.request.CreateCategoryRequest;

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
  public List<Category> categories() {
    return categoryService.getAllCategories();
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
}
