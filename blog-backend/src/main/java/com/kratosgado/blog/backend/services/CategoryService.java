package com.kratosgado.blog.backend.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kratosgado.blog.backend.cache.CacheConfig.CategoryCache;
import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.jdbc.CategoryRepository;
import com.kratosgado.blog.backend.utils.BlogUtils;
import com.kratosgado.blog.backend.utils.DtoMapper;
import com.kratosgado.blog.dtos.request.CreateCategoryRequest;
import com.kratosgado.blog.dtos.request.PageRequest;
import com.kratosgado.blog.dtos.response.CategoryResponse;
import com.kratosgado.blog.dtos.response.PageResponse;
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

  public Category createCategory(CreateCategoryRequest request) {
    String slug = BlogUtils.toSlug(request.name());
    if (categoryRepository.findBySlug(slug).isPresent()) {
      throw BlogException.conflict("Category with this name already exists");
    }

    Category category = Category.builder()
        .name(request.name())
        .slug(slug)
        .description(request.description())
        .build();
    return categoryRepository.save(category);

  }

  public Category updateCategory(Long categoryId, CreateCategoryRequest request) {
    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> BlogException.notFound("Category not found"));

    String slug = BlogUtils.toSlug(request.name());

    if (!category.getSlug().equals(slug) && categoryRepository.findBySlug(slug).isPresent()) {
      throw BlogException.conflict("Category with this name already exists");
    }

    category.setName(request.name());
    category.setSlug(slug);
    category.setDescription(request.description());

    return categoryRepository.update(category);

  }

  public void deleteCategory(Long categoryId) {
    categoryRepository.deleteById(categoryId);
  }

  public Category getCategoryById(Long categoryId) {
    // Try to get from cache first
    return categoryCache.get(categoryId).orElseGet(() -> {
      Category category = categoryRepository.findById(categoryId)
          .orElseThrow(() -> BlogException.notFound("Category not found"));
      // Cache the result
      categoryCache.put(categoryId, category);
      return category;

    });
  }

  public Category getCategoryBySlug(String slug) {
    return categoryRepository.findBySlug(slug)
        .orElseThrow(() -> BlogException.notFound("Category not found"));

  }

  public PageResponse<Category> getAllCategories(PageRequest pageRequest) {
    java.util.List<Category> categories = categoryRepository.findAll(pageRequest.getSize(), pageRequest.getOffset(),
        pageRequest.getSortBy(), pageRequest.getSortDir());
    Long total = categoryRepository.count();
    return DtoMapper.toPageResponse(categories, pageRequest.getPage(), pageRequest.getSize(), total.intValue());
  }

  public List<Category> getAllCategories() {
    return categoryRepository.findAll();
  }

  public List<CategoryResponse> getAllCategoriesWithPostCount() {
    return categoryRepository.findAllWithPostCount();
  }
}
