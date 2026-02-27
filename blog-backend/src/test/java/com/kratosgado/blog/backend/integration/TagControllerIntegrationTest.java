package com.kratosgado.blog.backend.integration;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.kratosgado.blog.backend.repositories.jpa.TagRepository;
import com.kratosgado.blog.backend.repositories.jpa.UserRepository;
import com.kratosgado.blog.dtos.request.CreateTagRequest;
import com.kratosgado.blog.dtos.request.UpdateTagRequest;
import com.kratosgado.blog.enums.UserRole;
import com.kratosgado.blog.backend.models.Tag;
import com.kratosgado.blog.backend.models.User;
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

/** Integration tests for TagController. Tests tag CRUD operations, search, and authorization. */
@DisplayName("TagController Integration Tests")
class TagControllerIntegrationTest extends BaseIntegrationTest {

  @Autowired private TagRepository tagRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  private User authorUser;
  private User readerUser;
  private Tag testTag;

  @BeforeEach
  @Override
  void baseSetUp() {
    // Clean up database
    tagRepository.deleteAll();
    userRepository.deleteAll();

    // Create test author
    authorUser = new User();
    authorUser.setEmail("author@example.com");
    authorUser.setUsername("author");
    authorUser.setPassword(passwordEncoder.encode("@Password123"));
    authorUser.setRole(UserRole.AUTHOR);
    authorUser = userRepository.save(authorUser);

    // Create test reader
    readerUser = new User();
    readerUser.setEmail("reader@example.com");
    readerUser.setUsername("reader");
    readerUser.setPassword(passwordEncoder.encode("@Password123"));
    readerUser.setRole(UserRole.READER);
    readerUser = userRepository.save(readerUser);

    // Create test tag
    testTag = new Tag();
    testTag.setName("Java");
    testTag.setSlug("java");
    testTag.setDescription("Java programming language");
    testTag = tagRepository.save(testTag);
  }

  @Nested
  @DisplayName("Create Tag Tests")
  class CreateTagTests {

    @Test
    @DisplayName("Should successfully create tag as authenticated user")
    void createTag_Authenticated_ShouldReturn201() throws Exception {
      CreateTagRequest request = new CreateTagRequest("Spring Boot", "Spring Boot framework");

      String token = generateToken(authorUser);

      mockMvc
          .perform(
              post("/v1/tags")
                  .header("Authorization", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.data.name", is("Spring Boot")))
          .andExpect(jsonPath("$.data.slug", is("spring-boot")))
          .andExpect(jsonPath("$.data.description", is("Spring Boot framework")));
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void createTag_Unauthenticated_ShouldReturn401() throws Exception {
      CreateTagRequest request = new CreateTagRequest("Python", "Python programming");

      mockMvc
          .perform(
              post("/v1/tags").contentType(MediaType.APPLICATION_JSON).content(toJson(request)))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 409 when tag name already exists")
    void createTag_DuplicateName_ShouldReturn409() throws Exception {
      CreateTagRequest request =
          new CreateTagRequest(
              "Java", // Already exists
              "Another Java description");

      String token = generateToken(authorUser);

      mockMvc
          .perform(
              post("/v1/tags")
                  .header("Authorization", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .andExpect(status().isConflict());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "A"}) // Empty, blank, or too short
    @DisplayName("Should return 400 for invalid tag name")
    void createTag_InvalidName_ShouldReturn400(String invalidName) throws Exception {
      CreateTagRequest request = new CreateTagRequest(invalidName, "Description");

      String token = generateToken(authorUser);

      mockMvc
          .perform(
              post("/v1/tags")
                  .header("Authorization", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("Update Tag Tests")
  class UpdateTagTests {

    @Test
    @DisplayName("Should successfully update tag")
    void updateTag_Valid_ShouldReturn200() throws Exception {
      UpdateTagRequest request = new UpdateTagRequest("Java SE", "Java Standard Edition");

      String token = generateToken(authorUser);

      mockMvc
          .perform(
              put("/v1/tags/" + testTag.getId())
                  .header("Authorization", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.name", is("Java SE")))
          .andExpect(jsonPath("$.data.description", is("Java Standard Edition")));
    }

    @Test
    @DisplayName("Should successfully update only name")
    void updateTag_OnlyName_ShouldReturn200() throws Exception {
      UpdateTagRequest request = new UpdateTagRequest("Java EE", null);

      String token = generateToken(authorUser);

      mockMvc
          .perform(
              put("/v1/tags/" + testTag.getId())
                  .header("Authorization", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.name", is("Java EE")))
          .andExpect(
              jsonPath(
                  "$.data.description", is("Java programming language"))); // Original description
    }

    @Test
    @DisplayName("Should return 404 for non-existent tag")
    void updateTag_NonExistent_ShouldReturn404() throws Exception {
      UpdateTagRequest request = new UpdateTagRequest("Updated Name", "Updated desc");

      String token = generateToken(authorUser);

      mockMvc
          .perform(
              put("/v1/tags/999999")
                  .header("Authorization", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void updateTag_Unauthenticated_ShouldReturn401() throws Exception {
      UpdateTagRequest request = new UpdateTagRequest("Updated", "Updated description");

      mockMvc
          .perform(
              put("/v1/tags/" + testTag.getId())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("Delete Tag Tests")
  class DeleteTagTests {

    @Test
    @DisplayName("Should successfully delete tag")
    void deleteTag_Valid_ShouldReturn200() throws Exception {
      String token = generateToken(authorUser);

      mockMvc
          .perform(delete("/v1/tags/" + testTag.getId()).header("Authorization", token))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 404 for non-existent tag")
    void deleteTag_NonExistent_ShouldReturn404() throws Exception {
      String token = generateToken(authorUser);

      mockMvc
          .perform(delete("/v1/tags/999999").header("Authorization", token))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void deleteTag_Unauthenticated_ShouldReturn401() throws Exception {
      mockMvc.perform(delete("/v1/tags/" + testTag.getId())).andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("Get Tag Tests")
  class GetTagTests {

    @Test
    @DisplayName("Should successfully get tag by ID")
    void getTag_ById_ShouldReturn200() throws Exception {
      mockMvc
          .perform(get("/v1/tags/" + testTag.getId()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.id", is(testTag.getId().intValue())))
          .andExpect(jsonPath("$.data.name", is("Java")))
          .andExpect(jsonPath("$.data.slug", is("java")));
    }

    @Test
    @DisplayName("Should successfully get tag by slug")
    void getTag_BySlug_ShouldReturn200() throws Exception {
      mockMvc
          .perform(get("/v1/tags/slug/" + testTag.getSlug()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.id", is(testTag.getId().intValue())))
          .andExpect(jsonPath("$.data.name", is("Java")))
          .andExpect(jsonPath("$.data.slug", is("java")));
    }

    @Test
    @DisplayName("Should return 404 for non-existent tag ID")
    void getTag_NonExistentId_ShouldReturn404() throws Exception {
      mockMvc.perform(get("/v1/tags/999999")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 for non-existent slug")
    void getTag_NonExistentSlug_ShouldReturn404() throws Exception {
      mockMvc.perform(get("/v1/tags/slug/non-existent")).andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("Get All Tags Tests")
  class GetAllTagsTests {

    @BeforeEach
    void setupMultipleTags() {
      // Create additional tags for testing
      for (int i = 1; i <= 15; i++) {
        Tag tag = new Tag();
        tag.setName("Tag " + i);
        tag.setSlug("tag-" + i);
        tag.setDescription("Description " + i);
        tagRepository.save(tag);
      }
    }

    @Test
    @DisplayName("Should get paginated list of tags")
    void getTags_Default_ShouldReturnPaginated() throws Exception {
      mockMvc
          .perform(get("/v1/tags").param("page", "0").param("size", "10"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.content", hasSize(10)))
          .andExpect(jsonPath("$.data.totalElements", greaterThanOrEqualTo(15)))
          .andExpect(jsonPath("$.data.currentPage", is(0)))
          .andExpect(jsonPath("$.data.pageSize", is(10)));
    }

    @ParameterizedTest
    @CsvSource({"0, 5, 5", "0, 10, 10", "1, 10, 6", "0, 20, 16"})
    @DisplayName("Should respect pagination parameters")
    void getTags_WithPagination_ShouldRespectParams(int page, int size, int expectedContentSize)
        throws Exception {
      mockMvc
          .perform(
              get("/v1/tags")
                  .param("page", String.valueOf(page))
                  .param("size", String.valueOf(size)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.currentPage", is(page)))
          .andExpect(jsonPath("$.data.pageSize", is(size)))
          .andExpect(jsonPath("$.data.content", hasSize(expectedContentSize)));
    }
  }

  @Nested
  @DisplayName("Search Tags Tests")
  class SearchTagsTests {

    @BeforeEach
    void setupSearchTags() {
      Tag python = new Tag();
      python.setName("Python");
      python.setSlug("python");
      python.setDescription("Python programming language");
      tagRepository.save(python);

      Tag javascript = new Tag();
      javascript.setName("JavaScript");
      javascript.setSlug("javascript");
      javascript.setDescription("JavaScript programming");
      tagRepository.save(javascript);
    }

    @Test
    @DisplayName("Should find tags by keyword")
    void searchTags_ByKeyword_ShouldReturnMatches() throws Exception {
      mockMvc
          .perform(get("/v1/tags/search").param("keyword", "Java"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))))
          .andExpect(jsonPath("$.data.content[0].name", containsStringIgnoringCase("java")));
    }

    @Test
    @DisplayName("Should return empty list for non-matching keyword")
    void searchTags_NoMatch_ShouldReturnEmpty() throws Exception {
      mockMvc
          .perform(get("/v1/tags/search").param("keyword", "NonExistentTag123"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.content", hasSize(0)));
    }
  }

  @Nested
  @DisplayName("Get Tags with Post Count Tests")
  class GetTagsWithPostCountTests {

    @Test
    @DisplayName("Should return empty list when no tags exist")
    void getTagsWithPostCount_NoTags_ShouldReturnEmpty() throws Exception {
      tagRepository.deleteAll();

      mockMvc
          .perform(get("/v1/tags/with-post-count"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data", isA(java.util.List.class)))
          .andExpect(jsonPath("$.data", hasSize(0)));
    }
  }
}
