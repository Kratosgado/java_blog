package com.kratosgado.blog.services;

import java.util.List;
import java.util.Optional;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.dao.CategoryDAO;
import com.kratosgado.blog.dtos.request.CreateCategoryDto;
import com.kratosgado.blog.dtos.request.UpdateCategoryDto;
import com.kratosgado.blog.models.Category;
import com.kratosgado.blog.utils.exceptions.BlogExceptions;
import com.kratosgado.blog.utils.validators.ValidatorEngine;

public class CategoryService {
  private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

  private final CategoryDAO categoryDAO;

  @Inject
  public CategoryService(CategoryDAO categoryDAO) {
    this.categoryDAO = categoryDAO;
  }

  public boolean createCategory(CreateCategoryDto dto) {
    ValidatorEngine.validate(dto);
    String slug = dto.name().toLowerCase().replaceAll("[^a-z0-9]+", "-");

    // Check if category already exists
    if (categoryDAO.getCategoryBySlug(slug).isPresent()) {
      throw BlogExceptions.conflict("Category with name '" + dto.name() + "' already exists");
    }

    Category category = Category.builder()
        .name(dto.name())
        .slug(slug)
        .description(dto.description())
        .build();

    boolean created = categoryDAO.createCategory(category);
    if (!created) {
      throw BlogExceptions.internal("Failed to create category");
    }
    logger.info("Category created: {}", dto.name());
    return true;
  }

  public boolean updateCategory(UpdateCategoryDto dto) {
    ValidatorEngine.validate(dto);
    Category existing = categoryDAO.getCategoryById(dto.id())
        .orElseThrow(() -> BlogExceptions.notFound("Category not found"));

    String slug = dto.name().toLowerCase().replaceAll("[^a-z0-9]+", "-");

    Category updated = Category.builder()
        .id(dto.id())
        .name(dto.name())
        .slug(slug)
        .description(dto.description())
        .createdAt(existing.getCreatedAt())
        .build();

    boolean success = categoryDAO.updateCategory(updated);
    if (!success) {
      throw BlogExceptions.internal("Failed to update category");
    }
    logger.info("Category updated: {}", dto.id());
    return true;
  }

  public boolean deleteCategory(int id) {
    if (categoryDAO.getCategoryById(id).isEmpty()) {
      throw BlogExceptions.notFound("Category not found");
    }

    boolean deleted = categoryDAO.deleteCategory(id);
    if (!deleted) {
      throw BlogExceptions.internal("Failed to delete category");
    }
    logger.info("Category deleted: {}", id);
    return true;
  }

  public Optional<Category> getCategoryById(int id) {
    return categoryDAO.getCategoryById(id);
  }

  public Optional<Category> getCategoryBySlug(String slug) {
    return categoryDAO.getCategoryBySlug(slug);
  }

  public List<Category> getAllCategories() {
    return categoryDAO.getAllCategories();
  }

  public List<Category> getCategoriesByPostId(int postId) {
    return categoryDAO.getCategoriesByPostId(postId);
  }

  public boolean addCategoryToPost(int postId, int categoryId) {
    return categoryDAO.addCategoryToPost(postId, categoryId);
  }

  public boolean removeCategoryFromPost(int postId, int categoryId) {
    return categoryDAO.removeCategoryFromPost(postId, categoryId);
  }

  public int getCategoryCount() {
    return categoryDAO.getCategoryCount();
  }
}
