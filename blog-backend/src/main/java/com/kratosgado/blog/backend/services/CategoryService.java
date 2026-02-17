package com.kratosgado.blog.backend.services;

import com.kratosgado.blog.backend.exceptions.ResourceAlreadyExistsException;
import com.kratosgado.blog.backend.exceptions.ResourceNotFoundException;
import com.kratosgado.blog.backend.repositories.jpa.CategoryRepository;
import com.kratosgado.blog.backend.utils.BlogUtils;
import com.kratosgado.blog.backend.utils.DtoMapper;
import com.kratosgado.blog.backend.utils.PageUtil;
import com.kratosgado.blog.dtos.request.CreateCategoryRequest;
import com.kratosgado.blog.dtos.request.PageRequest;
import com.kratosgado.blog.dtos.response.CategoryResponse;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.backend.models.Category;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
public class CategoryService {
  private final CategoryRepository categoryRepository;

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public Category createCategory(CreateCategoryRequest request) {
    String slug = BlogUtils.toSlug(request.name());
    if (categoryRepository.findBySlug(slug).isPresent()) {
      throw new ResourceAlreadyExistsException("Category with this name already exists");
    }

    Category category =
        Category.builder()
            .name(request.name())
            .slug(slug)
            .description(request.description())
            .build();
    return categoryRepository.save(category);
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public Category updateCategory(Long categoryId, CreateCategoryRequest request) {
    Category category =
        categoryRepository
            .findById(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

    String slug = BlogUtils.toSlug(request.name());

    if (!category.getSlug().equals(slug) && categoryRepository.findBySlug(slug).isPresent()) {
      throw new ResourceAlreadyExistsException("Category with this name already exists");
    }

    category.setName(request.name());
    category.setSlug(slug);
    category.setDescription(request.description());

    return categoryRepository.save(category);
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public void deleteCategory(Long categoryId) {
    if (!categoryRepository.existsById(categoryId)) {
      throw new ResourceNotFoundException("Category not found");
    }
    categoryRepository.deleteById(categoryId);
  }

  public Category getCategoryById(Long categoryId) {
    return categoryRepository
        .findById(categoryId)
        .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
  }

  public Category getCategoryBySlug(String slug) {
    return categoryRepository
        .findBySlug(slug)
        .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
  }

  public PageResponse<Category> getAllCategories(PageRequest pageRequest) {
    Page<Category> categoryPage = categoryRepository.findAll(PageUtil.toPageable(pageRequest));
    return DtoMapper.toPageResponse(categoryPage);
  }

  public List<Category> getAllCategories() {
    return categoryRepository.findAll();
  }

  public List<CategoryResponse> getAllCategoriesWithPostCount() {
    return categoryRepository.findAllWithPostCount();
  }
}
