package com.kratosgado.blog.backend.services;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.jpa.CategoryRepository;
import com.kratosgado.blog.dtos.request.CreateCategoryRequest;
import com.kratosgado.blog.dtos.request.PageRequest;
import com.kratosgado.blog.models.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService Tests")
class CategoryServiceTest {

  @Mock
  private CategoryRepository categoryRepository;

  @InjectMocks
  private CategoryService categoryService;

  private Category testCategory;
  private CreateCategoryRequest createRequest;

  @BeforeEach
  void setUp() {
    testCategory = Category.builder()
        .id(1L)
        .name("Technology")
        .slug("technology")
        .description("Tech related posts")
        .build();

    createRequest = new CreateCategoryRequest("Technology", "Tech related posts");
  }

  @Test
  @DisplayName("Should successfully create a category")
  void createCategory_WithValidData_ShouldReturnCategory() {
    // Arrange
    when(categoryRepository.findBySlug("technology")).thenReturn(Optional.empty());
    when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

    // Act
    Category result = categoryService.createCategory(createRequest);

    // Assert
    assertNotNull(result);
  }

  @Test
  @DisplayName("Should throw exception when creating duplicate category")
  void createCategory_WithExistingSlug_ShouldThrowException() {
    // Arrange
    when(categoryRepository.findBySlug("technology")).thenReturn(Optional.of(testCategory));

    // Act
    BlogException exception = assertThrows(BlogException.class,
        () -> categoryService.createCategory(createRequest));

    // Assert
    assertEquals("Category with this name already exists", exception.getMessage());
  }

  @ParameterizedTest
  @MethodSource("notFoundTestCases")
  @DisplayName("Should throw exception when resource not found")
  void notFound_ShouldThrowException(String operation) {
    // Arrange
    switch (operation) {
      case "updateCategory":
      case "getCategoryById":
        when(categoryRepository.findById(eq(1L))).thenReturn(Optional.empty());
        break;
      case "getCategoryBySlug":
        when(categoryRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());
        break;
    }

    // Act & Assert
    BlogException exception;
    switch (operation) {
      case "updateCategory":
        exception = assertThrows(BlogException.class,
            () -> categoryService.updateCategory(1L, createRequest));
        break;
      case "getCategoryById":
        exception = assertThrows(BlogException.class,
            () -> categoryService.getCategoryById(1L));
        break;
      case "getCategoryBySlug":
        exception = assertThrows(BlogException.class,
            () -> categoryService.getCategoryBySlug("nonexistent"));
        break;
      default:
        throw new IllegalArgumentException("Unknown operation: " + operation);
    }
    assertEquals("Category not found", exception.getMessage());
  }

  static Stream<Arguments> notFoundTestCases() {
    return Stream.of(
        Arguments.of("updateCategory"),
        Arguments.of("getCategoryById"),
        Arguments.of("getCategoryBySlug"));
  }

  @Test
  @DisplayName("Should successfully get category by ID")
  void getCategoryById_WithValidId_ShouldReturnCategory() {
    // Arrange
    when(categoryRepository.findById(eq(1L))).thenReturn(Optional.of(testCategory));

    // Act
    Category result = categoryService.getCategoryById(1L);

    // Assert
    assertNotNull(result);
    assertEquals(testCategory.getId(), result.getId());
  }

  @Test
  @DisplayName("Should successfully get category by slug")
  void getCategoryBySlug_WithValidSlug_ShouldReturnCategory() {
    // Arrange
    when(categoryRepository.findBySlug("technology")).thenReturn(Optional.of(testCategory));

    // Act
    Category result = categoryService.getCategoryBySlug("technology");

    // Assert
    assertNotNull(result);
    assertEquals(testCategory.getSlug(), result.getSlug());
  }

  @Test
  @DisplayName("Should successfully get all categories with pagination")
  void getAllCategories_ShouldReturnPageOfCategories() {
    // Arrange
    PageRequest pageRequest = PageRequest.builder().page(0).size(10).sortBy("id").sortDir("DESC").build();
    Page<Category> page = new PageImpl<>(List.of(testCategory));
    when(categoryRepository.findAll(any(Pageable.class))).thenReturn(page);

    // Act
    var result = categoryService.getAllCategories(pageRequest);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.totalElements());
  }
}
