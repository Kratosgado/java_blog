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
  public ResponseEntity<?> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
    try {
      Category category = categoryService.createCategory(request);
      return ResponseEntity.ok(category);
    } catch (Exception e) {
      logger.error("Failed to create category", e);
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> updateCategory(
    @PathVariable Long id,
    @Valid @RequestBody CreateCategoryRequest request
  ) {
    try {
      Category category = categoryService.updateCategory(id, request);
      return ResponseEntity.ok(category);
    } catch (Exception e) {
      logger.error("Failed to update category {}", id, e);
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
    try {
      categoryService.deleteCategory(id);
      return ResponseEntity.ok(Map.of("message", "Category deleted successfully"));
    } catch (Exception e) {
      logger.error("Failed to delete category {}", id, e);
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getCategory(@PathVariable Long id) {
    try {
      Category category = categoryService.getCategoryById(id);
      return ResponseEntity.ok(category);
    } catch (Exception e) {
      logger.error("Failed to get category {}", id, e);
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @GetMapping("/slug/{slug}")
  public ResponseEntity<?> getCategoryBySlug(@PathVariable String slug) {
    try {
      Category category = categoryService.getCategoryBySlug(slug);
      return ResponseEntity.ok(category);
    } catch (Exception e) {
      logger.error("Failed to get category by slug {}", slug, e);
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @GetMapping
  public ResponseEntity<?> getAllCategories() {
    try {
      List<Category> categories = categoryService.getAllCategories();
      return ResponseEntity.ok(categories);
    } catch (Exception e) {
      logger.error("Failed to get categories", e);
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }
}
