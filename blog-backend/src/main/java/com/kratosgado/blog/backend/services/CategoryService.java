package com.kratosgado.blog.backend.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kratosgado.blog.backend.cache.CacheConfig.CategoryCache;
import com.kratosgado.blog.backend.dao.CategoryDAO;
import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.dtos.request.CreateCategoryRequest;
import com.kratosgado.blog.models.Category;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CategoryService {
  private final CategoryDAO categoryDAO;
  private final CategoryCache categoryCache;

  public CategoryService(CategoryDAO categoryDAO, CategoryCache categoryCache) {
    this.categoryDAO = categoryDAO;
    this.categoryCache = categoryCache;
  }

  public Category createCategory(CreateCategoryRequest request) {
    String slug = generateSlug(request.name());

    if (categoryDAO.getCategoryBySlug(slug).isPresent()) {
      throw BlogException.conflict("Category with this name already exists");
    }

    Category category = Category.builder()
        .name(request.name())
        .slug(slug)
        .description(request.description())
        .build();

    return categoryDAO.createCategory(category)
        .orElseThrow(() -> BlogException.internal("Failed to create category"));
  }

  public Category updateCategory(Long categoryId, CreateCategoryRequest request) {
    Category category = categoryDAO.getCategoryById(categoryId.intValue())
        .orElseThrow(() -> BlogException.notFound("Category not found"));

    String slug = generateSlug(request.name());

    if (!category.getSlug().equals(slug) && categoryDAO.getCategoryBySlug(slug).isPresent()) {
      throw BlogException.conflict("Category with this name already exists");
    }

    category.setName(request.name());
    category.setSlug(slug);
    category.setDescription(request.description());

    return categoryDAO.updateCategory(category)
        .orElseThrow(() -> BlogException.internal("Failed to update category"));
  }

  public void deleteCategory(Long categoryId) {
    if (!categoryDAO.deleteCategory(categoryId.intValue())) {
      throw BlogException.internal("Failed to delete category");
    }
  }

  public Category getCategoryById(Long categoryId) {
    // Try to get from cache first
    return categoryCache.get(categoryId).orElseGet(() -> {
      log.debug("Cache miss for category ID: {}, fetching from database", categoryId);
      
      Category category = categoryDAO.getCategoryById(categoryId.intValue())
          .orElseThrow(() -> BlogException.notFound("Category not found"));
      
      // Cache the result
      categoryCache.put(categoryId, category);
      
      return category;
    });
  }

  public Category getCategoryBySlug(String slug) {
    return categoryDAO.getCategoryBySlug(slug)
        .orElseThrow(() -> BlogException.notFound("Category not found"));
  }

  public List<Category> getAllCategories() {
    return categoryDAO.getAllCategories();
  }

  private String generateSlug(String name) {
    return name.toLowerCase()
        .replaceAll("[^a-z0-9\\s-]", "")
        .replaceAll("\\s+", "-")
        .replaceAll("-+", "-")
        .trim();
  }
}

