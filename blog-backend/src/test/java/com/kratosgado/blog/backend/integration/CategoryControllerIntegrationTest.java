package com.kratosgado.blog.backend.integration;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.kratosgado.blog.backend.repositories.jpa.CategoryRepository;
import com.kratosgado.blog.backend.repositories.jpa.UserRepository;
import com.kratosgado.blog.dtos.request.CreateCategoryRequest;
import com.kratosgado.blog.enums.UserRole;
import com.kratosgado.blog.models.Category;
import com.kratosgado.blog.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Integration tests for CategoryController.
 * Tests category CRUD operations, pagination, and authorization.
 */
@DisplayName("CategoryController Integration Tests")
class CategoryControllerIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  private User adminUser;
  private User readerUser;
  private Category testCategory;

  @BeforeEach
  @Override
  void baseSetUp() {
    // Clean up database
    categoryRepository.deleteAll();
    userRepository.deleteAll();

    // Create test admin
    adminUser = new User();
    adminUser.setEmail("admin@example.com");
    adminUser.setUsername("admin");
    adminUser.setPassword(passwordEncoder.encode("password123"));
    adminUser.setRole(UserRole.ADMIN);
    adminUser = userRepository.save(adminUser);

    // Create test reader
    readerUser = new User();
    readerUser.setEmail("reader@example.com");
    readerUser.setUsername("reader");
    readerUser.setPassword(passwordEncoder.encode("password123"));
    readerUser.setRole(UserRole.READER);
    readerUser = userRepository.save(readerUser);

    // Create test category
    testCategory = new Category();
    testCategory.setName("Technology");
    testCategory.setSlug("technology");
    testCategory.setDescription("Tech related posts");
    testCategory = categoryRepository.save(testCategory);
  }

  @Nested
  @DisplayName("Create Category Tests")
  class CreateCategoryTests {

    @Test
    @DisplayName("Should successfully create category as authenticated user")
    void createCategory_Authenticated_ShouldReturn201() throws Exception {
      CreateCategoryRequest request = new CreateCategoryRequest(
          "Science",
          "Scientific articles and research"
      );

      String token = generateToken(adminUser.getId(), adminUser.getEmail(), UserRole.ADMIN);

      mockMvc.perform(post("/v1/categories")
              .header("Authorization", token)
              .contentType(MediaType.APPLICATION_JSON)
              .content(toJson(request)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.name", is("Science")))
          .andExpect(jsonPath("$.slug", is("science")))
          .andExpect(jsonPath("$.description", is("Scientific articles and research")));
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void createCategory_Unauthenticated_ShouldReturn401() throws Exception {
      CreateCategoryRequest request = new CreateCategoryRequest(
          "Science",
          "Scientific articles"
      );

      mockMvc.perform(post("/v1/categories")
              .contentType(MediaType.APPLICATION_JSON)
              .content(toJson(request)))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 400 when category name already exists")
    void createCategory_DuplicateName_ShouldReturn400() throws Exception {
      CreateCategoryRequest request = new CreateCategoryRequest(
          "Technology", // Already exists
          "Another tech category"
      );

      String token = generateToken(adminUser.getId(), adminUser.getEmail(), UserRole.ADMIN);

      mockMvc.perform(post("/v1/categories")
              .header("Authorization", token)
              .contentType(MediaType.APPLICATION_JSON)
              .content(toJson(request)))
          .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "AB"}) // Empty, blank, or too short
    @DisplayName("Should return 400 for invalid category name")
    void createCategory_InvalidName_ShouldReturn400(String invalidName) throws Exception {
      CreateCategoryRequest request = new CreateCategoryRequest(
          invalidName,
          "Description"
      );

      String token = generateToken(adminUser.getId(), adminUser.getEmail(), UserRole.ADMIN);

      mockMvc.perform(post("/v1/categories")
              .header("Authorization", token)
              .contentType(MediaType.APPLICATION_JSON)
              .content(toJson(request)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("Update Category Tests")
  class UpdateCategoryTests {

    @Test
    @DisplayName("Should successfully update category")
    void updateCategory_Valid_ShouldReturn200() throws Exception {
      CreateCategoryRequest request = new CreateCategoryRequest(
          "Updated Technology",
          "Updated description for tech"
      );

      String token = generateToken(adminUser.getId(), adminUser.getEmail(), UserRole.ADMIN);

      mockMvc.perform(put("/v1/categories/" + testCategory.getId())
              .header("Authorization", token)
              .contentType(MediaType.APPLICATION_JSON)
              .content(toJson(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.name", is("Updated Technology")))
          .andExpect(jsonPath("$.description", is("Updated description for tech")));
    }

    @Test
    @DisplayName("Should return 404 for non-existent category")
    void updateCategory_NonExistent_ShouldReturn404() throws Exception {
      CreateCategoryRequest request = new CreateCategoryRequest(
          "Updated Name",
          "Updated description"
      );

      String token = generateToken(adminUser.getId(), adminUser.getEmail(), UserRole.ADMIN);

      mockMvc.perform(put("/v1/categories/999999")
              .header("Authorization", token)
              .contentType(MediaType.APPLICATION_JSON)
              .content(toJson(request)))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void updateCategory_Unauthenticated_ShouldReturn401() throws Exception {
      CreateCategoryRequest request = new CreateCategoryRequest(
          "Updated Name",
          "Updated description"
      );

      mockMvc.perform(put("/v1/categories/" + testCategory.getId())
              .contentType(MediaType.APPLICATION_JSON)
              .content(toJson(request)))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("Delete Category Tests")
  class DeleteCategoryTests {

    @Test
    @DisplayName("Should successfully delete category")
    void deleteCategory_Valid_ShouldReturn200() throws Exception {
      String token = generateToken(adminUser.getId(), adminUser.getEmail(), UserRole.ADMIN);

      mockMvc.perform(delete("/v1/categories/" + testCategory.getId())
              .header("Authorization", token))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 404 for non-existent category")
    void deleteCategory_NonExistent_ShouldReturn404() throws Exception {
      String token = generateToken(adminUser.getId(), adminUser.getEmail(), UserRole.ADMIN);

      mockMvc.perform(delete("/v1/categories/999999")
              .header("Authorization", token))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void deleteCategory_Unauthenticated_ShouldReturn401() throws Exception {
      mockMvc.perform(delete("/v1/categories/" + testCategory.getId()))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("Get Category Tests")
  class GetCategoryTests {

    @Test
    @DisplayName("Should successfully get category by ID")
    void getCategory_ById_ShouldReturn200() throws Exception {
      mockMvc.perform(get("/v1/categories/" + testCategory.getId()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", is(testCategory.getId().intValue())))
          .andExpect(jsonPath("$.name", is("Technology")))
          .andExpect(jsonPath("$.slug", is("technology")));
    }

    @Test
    @DisplayName("Should successfully get category by slug")
    void getCategory_BySlug_ShouldReturn200() throws Exception {
      mockMvc.perform(get("/v1/categories/slug/" + testCategory.getSlug()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", is(testCategory.getId().intValue())))
          .andExpect(jsonPath("$.name", is("Technology")))
          .andExpect(jsonPath("$.slug", is("technology")));
    }

    @Test
    @DisplayName("Should return 404 for non-existent category ID")
    void getCategory_NonExistentId_ShouldReturn404() throws Exception {
      mockMvc.perform(get("/v1/categories/999999"))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 for non-existent slug")
    void getCategory_NonExistentSlug_ShouldReturn404() throws Exception {
      mockMvc.perform(get("/v1/categories/slug/non-existent"))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("Get All Categories Tests")
  class GetAllCategoriesTests {

    @BeforeEach
    void setupMultipleCategories() {
      // Create additional categories for pagination testing
      for (int i = 1; i <= 15; i++) {
        Category category = new Category();
        category.setName("Category " + i);
        category.setSlug("category-" + i);
        category.setDescription("Description " + i);
        categoryRepository.save(category);
      }
    }

    @Test
    @DisplayName("Should get paginated list of categories")
    void getCategories_Default_ShouldReturnPaginated() throws Exception {
      mockMvc.perform(get("/v1/categories")
              .param("page", "0")
              .param("size", "10"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content", hasSize(10)))
          .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(15)))
          .andExpect(jsonPath("$.currentPage", is(0)))
          .andExpect(jsonPath("$.pageSize", is(10)));
    }

    @ParameterizedTest
    @CsvSource({
        "0, 5, 5",
        "0, 10, 10",
        "1, 10, 6", // Second page should have remaining 6 items (16 total - 10 on first page)
        "0, 20, 16" // All items fit in one page
    })
    @DisplayName("Should respect pagination parameters")
    void getCategories_WithPagination_ShouldRespectParams(
        int page, int size, int expectedContentSize) throws Exception {
      mockMvc.perform(get("/v1/categories")
              .param("page", String.valueOf(page))
              .param("size", String.valueOf(size)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.currentPage", is(page)))
          .andExpect(jsonPath("$.pageSize", is(size)))
          .andExpect(jsonPath("$.content", hasSize(expectedContentSize)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"name", "createdAt"})
    @DisplayName("Should support sorting")
    void getCategories_WithSorting_ShouldSort(String sortBy) throws Exception {
      mockMvc.perform(get("/v1/categories")
              .param("sortBy", sortBy)
              .param("sortDir", "ASC"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content", notNullValue()));
    }
  }

  @Nested
  @DisplayName("Get Categories with Post Count Tests")
  class GetCategoriesWithPostCountTests {

    @Test
    @DisplayName("Should get all categories with post counts")
    void getCategoriesWithPostCount_ShouldReturnWithCounts() throws Exception {
      mockMvc.perform(get("/v1/categories/with-post-count"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", isA(java.util.List.class)))
          .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
          .andExpect(jsonPath("$[0].name", notNullValue()))
          .andExpect(jsonPath("$[0].postCount", notNullValue()));
    }

    @Test
    @DisplayName("Should return empty list when no categories exist")
    void getCategoriesWithPostCount_NoCategories_ShouldReturnEmpty() throws Exception {
      categoryRepository.deleteAll();

      mockMvc.perform(get("/v1/categories/with-post-count"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", isA(java.util.List.class)))
          .andExpect(jsonPath("$", hasSize(0)));
    }
  }
}
