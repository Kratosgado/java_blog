package com.kratosgado.blog.backend.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.dao.CategoryDAO;
import com.kratosgado.blog.backend.cache.CacheConfig.CategoryCache;
import com.kratosgado.blog.dtos.request.CreateCategoryRequest;
import com.kratosgado.blog.models.Category;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService Tests")
class CategoryServiceTest {

  @Mock
  private CategoryDAO categoryDAO;

  @Mock
  private CategoryCache categoryCache;

  @InjectMocks
  private CategoryService categoryService;

  private Category testCategory;
  private CreateCategoryRequest createRequest;

  @BeforeEach
  void setUp() {
    testCategory = Category.builder()
        .id(1)
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
    when(categoryDAO.getCategoryBySlug("technology")).thenReturn(Optional.empty());
    when(categoryDAO.createCategory(any(Category.class))).thenReturn(Optional.of(testCategory));

    // Act
    Category result = categoryService.createCategory(createRequest);

    // Assert
    assertNotNull(result);
  }

  @Test
  @DisplayName("Should throw exception when creating duplicate category")
  void createCategory_WithExistingSlug_ShouldThrowException() {
    // Arrange
    when(categoryDAO.getCategoryBySlug("technology")).thenReturn(Optional.of(testCategory));

    // Act
    BlogException exception = assertThrows(BlogException.class,
        () -> categoryService.createCategory(createRequest));

    // Assert
    assertEquals("Category with this name already exists", exception.getMessage());
  }

  @ParameterizedTest
  @MethodSource("slugGenerationTestCases")
  @DisplayName("Should generate correct slug from category name")
  void createCategory_ShouldGenerateCorrectSlug(String name, String expectedSlug) {
    // Arrange
    CreateCategoryRequest request = new CreateCategoryRequest(name, "Description");
    when(categoryDAO.getCategoryBySlug(anyString())).thenReturn(Optional.empty());
    when(categoryDAO.createCategory(any(Category.class))).thenAnswer(invocation -> {
      Category category = invocation.getArgument(0);
      assertEquals(expectedSlug, category.getSlug());
      return Optional.of(category);
    });

    // Act
    categoryService.createCategory(request);

    // Assert - verification done in mock answer
  }

  static Stream<Arguments> slugGenerationTestCases() {
    return Stream.of(
        Arguments.of("Web Development & Design", "web-development-design"),
        Arguments.of("Tech & AI/ML (2024)!", "tech-aiml-2024"));
  }

  @Test
  @DisplayName("Should successfully update a category")
  void updateCategory_WithValidData_ShouldReturnUpdatedCategory() {
    // Arrange
    CreateCategoryRequest updateRequest = new CreateCategoryRequest(
        "Updated Tech",
        "Updated description");
    when(categoryDAO.getCategoryById(1)).thenReturn(Optional.of(testCategory));
    when(categoryDAO.getCategoryBySlug("updated-tech")).thenReturn(Optional.empty());
    when(categoryDAO.updateCategory(any(Category.class))).thenReturn(Optional.of(testCategory));

    // Act
    Category result = categoryService.updateCategory(1L, updateRequest);

    // Assert
    assertNotNull(result);
    assertEquals("Updated Tech", testCategory.getName());
    assertEquals("updated-tech", testCategory.getSlug());
  }

  @Test
  @DisplayName("Should allow updating category with same name")
  void updateCategory_WithSameName_ShouldSucceed() {
    // Arrange
    CreateCategoryRequest sameNameRequest = new CreateCategoryRequest(
        "Technology",
        "Updated description");
    when(categoryDAO.getCategoryById(1)).thenReturn(Optional.of(testCategory));
    when(categoryDAO.updateCategory(any(Category.class))).thenReturn(Optional.of(testCategory));

    // Act
    Category result = categoryService.updateCategory(1L, sameNameRequest);

    // Assert
    assertNotNull(result);
    verify(categoryDAO, never()).getCategoryBySlug(anyString());
  }

  @Test
  @DisplayName("Should throw exception when updating to existing category name")
  void updateCategory_WithExistingSlug_ShouldThrowException() {
    // Arrange
    CreateCategoryRequest updateRequest = new CreateCategoryRequest(
        "Programming",
        "Programming posts");
    when(categoryDAO.getCategoryById(1)).thenReturn(Optional.of(testCategory));
    when(categoryDAO.getCategoryBySlug("programming")).thenReturn(Optional.of(new Category()));

    // Act
    BlogException exception = assertThrows(BlogException.class,
        () -> categoryService.updateCategory(1L, updateRequest));

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
        when(categoryDAO.getCategoryById(1)).thenReturn(Optional.empty());
        when(categoryCache.get(1L)).thenReturn(Optional.empty());
        break;
      case "getCategoryBySlug":
        when(categoryDAO.getCategoryBySlug("nonexistent")).thenReturn(Optional.empty());
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
  @DisplayName("Should successfully delete a category")
  void deleteCategory_WithValidId_ShouldDeleteCategory() {
    // Arrange
    when(categoryDAO.deleteCategory(1)).thenReturn(true);

    // Act
    categoryService.deleteCategory(1L);

    // Assert - method completes without exception
  }

  @Test
  @DisplayName("Should successfully get category by ID")
  void getCategoryById_WithValidId_ShouldReturnCategory() {
    // Arrange
    when(categoryCache.get(1L)).thenReturn(Optional.empty()); // Cache miss
    when(categoryDAO.getCategoryById(1)).thenReturn(Optional.of(testCategory));

    // Act
    Category result = categoryService.getCategoryById(1L);

    // Assert
    assertNotNull(result);
    assertEquals(testCategory.getId(), result.getId());
    verify(categoryCache).put(1L, testCategory); // Verify caching
  }

  @Test
  @DisplayName("Should successfully get category by slug")
  void getCategoryBySlug_WithValidSlug_ShouldReturnCategory() {
    // Arrange
    when(categoryDAO.getCategoryBySlug("technology")).thenReturn(Optional.of(testCategory));

    // Act
    Category result = categoryService.getCategoryBySlug("technology");

    // Assert
    assertNotNull(result);
    assertEquals(testCategory.getSlug(), result.getSlug());
  }

  @Test
  @DisplayName("Should successfully get all categories")
  void getAllCategories_ShouldReturnListOfCategories() {
    // Arrange
    List<Category> categories = List.of(testCategory);
    when(categoryDAO.getAllCategories()).thenReturn(categories);

    // Act
    List<Category> result = categoryService.getAllCategories();

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
  }
}
