package com.kratosgado.blog.backend.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kratosgado.blog.backend.cache.CacheConfig.CategoryCache;
import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.jpa.CategoryRepository;
import com.kratosgado.blog.dtos.request.CreateCategoryRequest;
import com.kratosgado.blog.models.Category;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CategoryService {
  private final CategoryRepository categoryRepository;
  private final CategoryCache categoryCache;

  public CategoryService(CategoryRepository categoryRepository, CategoryCache categoryCache) {
    this.categoryRepository = categoryRepository;
    this.categoryCache = categoryCache;
  }

  @Transactional
  public Category createCategory(CreateCategoryRequest request) {
    String slug = generateSlug(request.name());

    if (categoryRepository.existsBySlug(slug)) {
      throw BlogException.conflict("Category with this name already exists");
    }

    Category category = Category.builder()
        .name(request.name())
        .slug(slug)
        .description(request.description())
        .build();

    Category saved = categoryRepository.save(category);
    
    // Add to cache
    categoryCache.put(saved.getId(), saved);
    log.debug("Created category with ID: {} and added to cache", saved.getId());
    
    return saved;
  }

  @Transactional
  public Category updateCategory(Long categoryId, CreateCategoryRequest request) {
    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> BlogException.notFound("Category not found"));

    String slug = generateSlug(request.name());

    if (!category.getSlug().equals(slug) && categoryRepository.existsBySlug(slug)) {
      throw BlogException.conflict("Category with this name already exists");
    }

    category.setName(request.name());
    category.setSlug(slug);
    category.setDescription(request.description());

    Category updated = categoryRepository.save(category);
    
    // Update cache
    categoryCache.put(updated.getId(), updated);
    log.debug("Updated category with ID: {} in cache", updated.getId());
    
    return updated;
  }

  @Transactional
  public void deleteCategory(Long categoryId) {
    categoryRepository.deleteById(categoryId);
    
    // Evict from cache
    categoryCache.evict(categoryId);
    log.debug("Deleted category with ID: {} and evicted from cache", categoryId);
  }

  public Category getCategoryById(Long categoryId) {
    // Try to get from cache first
    return categoryCache.get(categoryId).orElseGet(() -> {
      log.debug("Cache miss for category ID: {}, fetching from database", categoryId);
      Category category = categoryRepository.findById(categoryId)
          .orElseThrow(() -> BlogException.notFound("Category not found"));
      
      // Add to cache
      categoryCache.put(categoryId, category);
      return category;
    });
  }

  public Category getCategoryBySlug(String slug) {
    log.debug("Searching for category by slug: {} in cache", slug);
    
    // Search cache for category with matching slug
    return categoryCache.getAll().stream()
        .filter(cat -> cat.getSlug().equals(slug))
        .findFirst()
        .orElseGet(() -> {
          log.debug("Cache miss for category slug: {}, fetching from database", slug);
          Category category = categoryRepository.findBySlug(slug)
              .orElseThrow(() -> BlogException.notFound("Category not found"));
          
          // Add to cache
          categoryCache.put(category.getId(), category);
          return category;
        });
  }

  public List<Category> getAllCategories() {
    log.debug("Getting all categories from cache");
    return categoryCache.getAll();
  }

  private String generateSlug(String name) {
    return name.toLowerCase()
        .replaceAll("[^a-z0-9\\s-]", "")
        .replaceAll("\\s+", "-")
        .replaceAll("-+", "-")
        .trim();
  }
}
