package com.kratosgado.blog.backend.services;

import com.kratosgado.blog.backend.models.Category;
import com.kratosgado.blog.dtos.request.CreateCategoryRequest;
import com.kratosgado.blog.dtos.request.PageRequest;
import com.kratosgado.blog.dtos.response.CategoryResponse;
import com.kratosgado.blog.dtos.response.PageResponse;
import java.util.List;

public interface CategoryService {

  Category createCategory(CreateCategoryRequest request);

  Category updateCategory(Long categoryId, CreateCategoryRequest request);

  void deleteCategory(Long categoryId);

  Category getCategoryById(Long categoryId);

  Category getCategoryBySlug(String slug);

  PageResponse<Category> getAllCategories(PageRequest pageRequest);

  List<Category> getAllCategories();

  List<CategoryResponse> getAllCategoriesWithPostCount();
}
