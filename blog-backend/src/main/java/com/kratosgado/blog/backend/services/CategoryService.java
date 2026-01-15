package com.kratosgado.blog.backend.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.jpa.CategoryRepository;
import com.kratosgado.blog.dtos.request.CreateCategoryRequest;
import com.kratosgado.blog.models.Category;

@Service
public class CategoryService {
  private final CategoryRepository categoryRepository;

  public CategoryService(CategoryRepository categoryRepository) {
    this.categoryRepository = categoryRepository;
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

    return categoryRepository.save(category);
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

    return categoryRepository.save(category);
  }

  @Transactional
  public void deleteCategory(Long categoryId) {
    categoryRepository.deleteById(categoryId);
  }

  public Category getCategoryById(Long categoryId) {
    return categoryRepository.findById(categoryId)
        .orElseThrow(() -> BlogException.notFound("Category not found"));
  }

  public Category getCategoryBySlug(String slug) {
    return categoryRepository.findBySlug(slug)
        .orElseThrow(() -> BlogException.notFound("Category not found"));
  }

  public List<Category> getAllCategories() {
    return categoryRepository.findAll();
  }

  private String generateSlug(String name) {
    return name.toLowerCase()
        .replaceAll("[^a-z0-9\\s-]", "")
        .replaceAll("\\s+", "-")
        .replaceAll("-+", "-")
        .trim();
  }
}
