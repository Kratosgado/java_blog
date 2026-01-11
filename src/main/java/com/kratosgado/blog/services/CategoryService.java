package com.kratosgado.blog.services;

import java.util.List;
import java.util.Optional;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.dao.CategoryDAO;
import com.kratosgado.blog.models.Category;
import com.kratosgado.blog.utils.exceptions.BlogExceptions;

public class CategoryService {
  private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

  private final CategoryDAO categoryDAO;

  @Inject
  public CategoryService(CategoryDAO categoryDAO) {
    this.categoryDAO = categoryDAO;
  }

  public boolean createCategory(String name, String description) {
    String slug = name.toLowerCase().replaceAll("[^a-z0-9]+", "-");

    // Check if category already exists
    if (categoryDAO.getCategoryBySlug(slug).isPresent()) {
      throw BlogExceptions.conflict("Category with name '" + name + "' already exists");
    }

    Category category = Category.builder()
        .name(name)
        .slug(slug)
        .description(description)
        .build();

    boolean created = categoryDAO.createCategory(category);
    if (!created) {
      throw BlogExceptions.internal("Failed to create category");
    }
    logger.info("Category created: {}", name);
    return true;
  }

  public boolean updateCategory(int id, String name, String description) {
    Category existing = categoryDAO.getCategoryById(id)
        .orElseThrow(() -> BlogExceptions.notFound("Category not found"));

    String slug = name.toLowerCase().replaceAll("[^a-z0-9]+", "-");

    Category updated = Category.builder()
        .id(id)
        .name(name)
        .slug(slug)
        .description(description)
        .createdAt(existing.getCreatedAt())
        .build();

    boolean success = categoryDAO.updateCategory(updated);
    if (!success) {
      throw BlogExceptions.internal("Failed to update category");
    }
    logger.info("Category updated: {}", id);
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
