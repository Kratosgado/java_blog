package com.kratosgado.blog.backend.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kratosgado.blog.backend.services.CategoryService;
import com.kratosgado.blog.dtos.request.CreateCategoryRequest;
import com.kratosgado.blog.models.Category;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
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
    categoryService.deleteCategory(id);
    return "Category deleted successfully";
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
