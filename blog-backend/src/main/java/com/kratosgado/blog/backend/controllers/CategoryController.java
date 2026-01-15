package com.kratosgado.blog.backend.controllers;

import com.kratosgado.blog.dtos.request.CreateCategoryRequest;
import com.kratosgado.blog.models.Category;
import com.kratosgado.blog.backend.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

  private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

  private final CategoryService categoryService;

  @PostMapping
  public Category createCategory(@Valid @RequestBody CreateCategoryRequest request) {
    return categoryService.createCategory(request);
  }

  @PutMapping("/{id}")
  public Category updateCategory(
      @PathVariable("id") Long id,
      @Valid @RequestBody CreateCategoryRequest request) {
    return categoryService.updateCategory(id, request);
  }

  @DeleteMapping("/{id}")
  public String deleteCategory(@PathVariable("id") Long id) {
    return "Category deleted successfully";
    // logger.info("Deleting category ID: {}", id);
    // categoryService.deleteCategory(id);

  }

  @GetMapping("/{id}")
  public Category getCategory(@PathVariable("id") Long id) {
    return categoryService.getCategoryById(id);

  }

  @GetMapping("/slug/{slug}")
  public Category getCategoryBySlug(@PathVariable String slug) {
    return categoryService.getCategoryBySlug(slug);
  }

  @GetMapping
  public List<Category> getAllCategories() {
    return categoryService.getAllCategories();
  }
}
