package com.kratosgado.blog.backend.services;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.jpa.CategoryRepository;
import com.kratosgado.blog.backend.utils.BlogUtils;
import com.kratosgado.blog.dtos.response.CategoryResponse;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.models.Category;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional(readOnly = true)
public class CategoryService {
  private final CategoryRepository categoryRepository;

  public CategoryService(CategoryRepository categoryRepository) {
    this.categoryRepository = categoryRepository;
  }

  @Transactional
  @CacheEvict(value = "categories", allEntries = true)
  public Category createCategory(com.kratosgado.blog.dtos.request.CreateCategoryRequest request) {
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

  @Transactional
  @CacheEvict(value = "categories", allEntries = true)
  public Category updateCategory(Long categoryId, com.kratosgado.blog.dtos.request.CreateCategoryRequest request) {
    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> BlogException.notFound("Category not found"));

    String slug = BlogUtils.toSlug(request.name());

    if (!category.getSlug().equals(slug) && categoryRepository.findBySlug(slug).isPresent()) {
      throw BlogException.conflict("Category with this name already exists");
    }

    category.setName(request.name());
    category.setSlug(slug);
    category.setDescription(request.description());

    return categoryRepository.save(category);
  }

  @Transactional
  @CacheEvict(value = "categories", allEntries = true)
  public void deleteCategory(Long categoryId) {
    categoryRepository.deleteById(categoryId);
  }

  @Cacheable(value = "categories", key = "#categoryId")
  public Category getCategoryById(Long categoryId) {
    return categoryRepository.findById(categoryId)
        .orElseThrow(() -> BlogException.notFound("Category not found"));
  }

  @Cacheable(value = "categories", key = "#slug")
  public Category getCategoryBySlug(String slug) {
    return categoryRepository.findBySlug(slug)
        .orElseThrow(() -> BlogException.notFound("Category not found"));
  }

  public PageResponse<Category> getAllCategories(com.kratosgado.blog.dtos.request.PageRequest pageRequest) {
    Sort sort = Sort.by(Sort.Direction.fromString(pageRequest.getSortDir()), pageRequest.getSortBy());
    Pageable pageable = PageRequest.of(pageRequest.getPage(), pageRequest.getSize(), sort);
    Page<Category> categoryPage = categoryRepository.findAll(pageable);

    return new PageResponse<>(
        categoryPage.getContent(),
        categoryPage.getNumber(),
        categoryPage.getSize(),
        (int) categoryPage.getTotalElements(),
        categoryPage.getTotalPages(),
        categoryPage.isFirst(),
        categoryPage.isLast());
  }

  public List<Category> getAllCategories() {
    return categoryRepository.findAll();
  }

  public List<CategoryResponse> getAllCategoriesWithPostCount() {
    return categoryRepository.findAllWithPostCount();
  }
}
