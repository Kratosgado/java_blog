package com.kratosgado.blog.backend.integration;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.kratosgado.blog.backend.repositories.jpa.CategoryRepository;
import com.kratosgado.blog.backend.repositories.jpa.PostRepository;
import com.kratosgado.blog.backend.repositories.jpa.TagRepository;
import com.kratosgado.blog.backend.repositories.jpa.UserRepository;
import com.kratosgado.blog.dtos.request.CreatePostRequest;
import com.kratosgado.blog.dtos.request.UpdatePostRequest;
import com.kratosgado.blog.enums.PostStatus;
import com.kratosgado.blog.enums.UserRole;
import com.kratosgado.blog.backend.models.Category;
import com.kratosgado.blog.backend.models.Post;
import com.kratosgado.blog.backend.models.Tag;
import com.kratosgado.blog.backend.models.User;
import java.util.List;
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
 * Integration tests for PostController. Tests post CRUD operations, search, filtering, and
 * authorization.
 */
@DisplayName("PostController Integration Tests")
class PostControllerIntegrationTest extends BaseIntegrationTest {

  @Autowired private PostRepository postRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private CategoryRepository categoryRepository;

  @Autowired private TagRepository tagRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  private User authorUser;
  private User readerUser;
  private Category testCategory;
  private Tag testTag;
  private Post testPost;

  @BeforeEach
  @Override
  void baseSetUp() {
    // Clean up database
    postRepository.deleteAll();
    tagRepository.deleteAll();
    categoryRepository.deleteAll();
    userRepository.deleteAll();

    // Create test author
    authorUser = new User();
    authorUser.setEmail("author@example.com");
    authorUser.setUsername("author");
    authorUser.setPassword(passwordEncoder.encode("@Password123"));
    authorUser.setRole(UserRole.AUTHOR);
    authorUser.setAvatarUrl("https://example.com/author.png");
    authorUser = userRepository.save(authorUser);

    // Create test reader
    readerUser = new User();
    readerUser.setEmail("reader@example.com");
    readerUser.setUsername("reader");
    readerUser.setPassword(passwordEncoder.encode("@Password123"));
    readerUser.setRole(UserRole.READER);
    readerUser = userRepository.save(readerUser);

    // Create test category
    testCategory = new Category();
    testCategory.setName("Test Category");
    testCategory.setSlug("test-category");
    testCategory.setDescription("Test category description");
    testCategory = categoryRepository.save(testCategory);

    // Create test tag
    testTag = new Tag();
    testTag.setName("Test Tag");
    testTag.setSlug("test-tag");
    testTag = tagRepository.save(testTag);

    // Create test post
    testPost = new Post();
    testPost.setTitle("Test Post");
    testPost.setSlug("test-post");
    testPost.setContent("This is test post content");
    testPost.setExcerpt("Test excerpt");
    testPost.setStatus(PostStatus.published);
    testPost.setUser(authorUser);
    testPost.setCategory(testCategory);
    testPost.setTags(List.of(testTag));
    testPost = postRepository.save(testPost);
  }

  @Nested
  @DisplayName("Create Post Tests")
  class CreatePostTests {

    @Test
    @DisplayName("Should successfully create post with AUTHOR role")
    void createPost_AsAuthor_ShouldReturn201() throws Exception {
      CreatePostRequest request =
          new CreatePostRequest(
              "New Post Title",
              "This is the content of the new post",
              "Brief excerpt",
              testCategory.getId(),
              "https://example.com/cover.jpg",
              "draft",
              new Long[] {testTag.getId()});

      String token = generateToken(authorUser);

      mockMvc
          .perform(
              post("/v1/posts")
                  .header("Authorization", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.data.title", is("New Post Title")))
          .andExpect(jsonPath("$.data.content", is("This is the content of the new post")))
          .andExpect(jsonPath("$.data.status", is("draft")))
          .andExpect(jsonPath("$.data.user.id", is(authorUser.getId().intValue())));
    }

    @Test
    @DisplayName("Should return 403 when READER tries to create post")
    void createPost_AsReader_ShouldReturn403() throws Exception {
      CreatePostRequest request =
          new CreatePostRequest(
              "New Post", "Content", "Excerpt", testCategory.getId(), null, "draft", new Long[] {});

      String token = generateToken(readerUser);

      mockMvc
          .perform(
              post("/v1/posts")
                  .header("Authorization", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void createPost_Unauthenticated_ShouldReturn401() throws Exception {
      CreatePostRequest request =
          new CreatePostRequest(
              "New Post", "Content", "Excerpt", testCategory.getId(), null, "draft", new Long[] {});

      mockMvc
          .perform(
              post("/v1/posts").contentType(MediaType.APPLICATION_JSON).content(toJson(request)))
          .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @CsvSource({
      "'', Content, Excerpt", // Empty title
      "Title, '', Excerpt", // Empty content
      "AB, Content, Excerpt" // Title too short
    })
    @DisplayName("Should return 400 for invalid post data")
    void createPost_WithInvalidData_ShouldReturn400(String title, String content, String excerpt)
        throws Exception {
      CreatePostRequest request =
          new CreatePostRequest(
              title, content, excerpt, testCategory.getId(), null, "draft", new Long[] {});

      String token = generateToken(authorUser);

      mockMvc
          .perform(
              post("/v1/posts")
                  .header("Authorization", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("Update Post Tests")
  class UpdatePostTests {

    @Test
    @DisplayName("Should successfully update own post")
    void updatePost_OwnPost_ShouldReturn200() throws Exception {
      UpdatePostRequest request =
          new UpdatePostRequest(
              "Updated Title",
              "Updated content",
              "Updated excerpt",
              testCategory.getId(),
              "https://example.com/new-cover.jpg",
              PostStatus.published,
              new Long[] {testTag.getId()});

      String token = generateToken(authorUser);

      mockMvc
          .perform(
              put("/v1/posts/" + testPost.getId())
                  .header("Authorization", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.title", is("Updated Title")))
          .andExpect(jsonPath("$.data.content", is("Updated content")))
          .andExpect(jsonPath("$.data.status", is("published")));
    }

    @Test
    @DisplayName("Should return 403 when updating another user's post")
    void updatePost_OtherUsersPost_ShouldReturn403() throws Exception {
      UpdatePostRequest request =
          new UpdatePostRequest(
              "Updated Title",
              "Updated content",
              "Updated excerpt",
              testCategory.getId(),
              null,
              PostStatus.published,
              new Long[] {});

      String token = generateToken(readerUser);

      mockMvc
          .perform(
              put("/v1/posts/" + testPost.getId())
                  .header("Authorization", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 404 for non-existent post")
    void updatePost_NonExistentPost_ShouldReturn404() throws Exception {
      UpdatePostRequest request =
          new UpdatePostRequest(
              "Updated Title",
              "Updated content",
              "Updated excerpt",
              testCategory.getId(),
              null,
              PostStatus.published,
              new Long[] {});

      String token = generateToken(authorUser);

      mockMvc
          .perform(
              put("/v1/posts/999999")
                  .header("Authorization", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("Publish Post Tests")
  class PublishPostTests {

    @Test
    @DisplayName("Should successfully publish draft post")
    void publishPost_DraftPost_ShouldReturn200() throws Exception {
      // Create a draft post
      Post draftPost = new Post();
      draftPost.setTitle("Draft Post");
      draftPost.setSlug("draft-post");
      draftPost.setContent("Draft content");
      draftPost.setExcerpt("Draft excerpt");
      draftPost.setStatus(PostStatus.draft);
      draftPost.setUser(authorUser);
      draftPost.setCategory(testCategory);
      draftPost = postRepository.save(draftPost);

      String token = generateToken(authorUser);

      mockMvc
          .perform(
              put("/v1/posts/" + draftPost.getId() + "/publish").header("Authorization", token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.status", is("published")));
    }

    @Test
    @DisplayName("Should return 403 when publishing another user's post")
    void publishPost_OtherUsersPost_ShouldReturn403() throws Exception {
      String token = generateToken(readerUser);

      mockMvc
          .perform(put("/v1/posts/" + testPost.getId() + "/publish").header("Authorization", token))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  @DisplayName("Delete Post Tests")
  class DeletePostTests {

    @Test
    @DisplayName("Should successfully delete own post")
    void deletePost_OwnPost_ShouldReturn200() throws Exception {
      String token = generateToken(authorUser);

      mockMvc
          .perform(delete("/v1/posts/" + testPost.getId()).header("Authorization", token))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 403 when deleting another user's post")
    void deletePost_OtherUsersPost_ShouldReturn403() throws Exception {
      String token = generateToken(readerUser);

      mockMvc
          .perform(delete("/v1/posts/" + testPost.getId()).header("Authorization", token))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void deletePost_Unauthenticated_ShouldReturn401() throws Exception {
      mockMvc.perform(delete("/v1/posts/" + testPost.getId())).andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("Get Post Tests")
  class GetPostTests {

    @Test
    @DisplayName("Should successfully get post by ID")
    void getPost_ById_ShouldReturn200() throws Exception {
      mockMvc
          .perform(get("/v1/posts/" + testPost.getId()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.id", is(testPost.getId().intValue())))
          .andExpect(jsonPath("$.data.title", is("Test Post")))
          .andExpect(jsonPath("$.data.slug", is("test-post")));
    }

    @Test
    @DisplayName("Should successfully get post by slug")
    void getPost_BySlug_ShouldReturn200() throws Exception {
      mockMvc
          .perform(get("/v1/posts/slug/" + testPost.getSlug()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.id", is(testPost.getId().intValue())))
          .andExpect(jsonPath("$.data.title", is("Test Post")))
          .andExpect(jsonPath("$.data.slug", is("test-post")));
    }

    @Test
    @DisplayName("Should return 404 for non-existent post ID")
    void getPost_NonExistentId_ShouldReturn404() throws Exception {
      mockMvc.perform(get("/v1/posts/999999")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 for non-existent slug")
    void getPost_NonExistentSlug_ShouldReturn404() throws Exception {
      mockMvc.perform(get("/v1/posts/slug/non-existent-slug")).andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("Get All Posts Tests")
  class GetAllPostsTests {

    @Test
    @DisplayName("Should get paginated list of published posts")
    void getPosts_Default_ShouldReturnPublished() throws Exception {
      mockMvc
          .perform(get("/v1/posts").param("page", "0").param("size", "10"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))))
          .andExpect(jsonPath("$.data.totalElements", greaterThanOrEqualTo(1)))
          .andExpect(jsonPath("$.data.content[0].title", notNullValue()));
    }

    @ParameterizedTest
    @CsvSource({"0, 5", "0, 10", "0, 20", "1, 10"})
    @DisplayName("Should respect pagination parameters")
    void getPosts_WithPagination_ShouldRespectParams(int page, int size) throws Exception {
      mockMvc
          .perform(
              get("/v1/posts")
                  .param("page", String.valueOf(page))
                  .param("size", String.valueOf(size)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.currentPage", is(page)))
          .andExpect(jsonPath("$.data.pageSize", is(size)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"title", "createdAt", "updatedAt"})
    @DisplayName("Should support sorting by different fields")
    void getPosts_WithSorting_ShouldSort(String sortBy) throws Exception {
      mockMvc
          .perform(get("/v1/posts").param("sortBy", sortBy).param("sortDir", "ASC"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.content", notNullValue()));
    }
  }

  @Nested
  @DisplayName("Search Posts Tests")
  class SearchPostsTests {

    @Test
    @DisplayName("Should find posts by keyword in title")
    void searchPosts_ByTitle_ShouldReturnMatches() throws Exception {
      mockMvc
          .perform(get("/v1/posts/search").param("keyword", "Test"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))))
          .andExpect(jsonPath("$.data.content[0].title", containsString("Test")));
    }

    @Test
    @DisplayName("Should return empty list for non-matching keyword")
    void searchPosts_NoMatch_ShouldReturnEmpty() throws Exception {
      mockMvc
          .perform(get("/v1/posts/search").param("keyword", "NonExistentKeyword123"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.content", hasSize(0)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"test", "TEST", "TeSt"})
    @DisplayName("Should be case-insensitive")
    void searchPosts_CaseInsensitive_ShouldReturnMatches(String keyword) throws Exception {
      mockMvc
          .perform(get("/v1/posts/search").param("keyword", keyword))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))));
    }
  }

  @Nested
  @DisplayName("Get Posts by User Tests")
  class GetPostsByUserTests {

    @Test
    @DisplayName("Should get posts by user ID")
    void getUserPosts_ValidUser_ShouldReturnPosts() throws Exception {
      mockMvc
          .perform(get("/v1/posts/user/" + authorUser.getId()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))))
          .andExpect(jsonPath("$.data.content[0].title", notNullValue()));
    }

    @Test
    @DisplayName("Should return empty list for user with no posts")
    void getUserPosts_UserWithNoPosts_ShouldReturnEmpty() throws Exception {
      mockMvc
          .perform(get("/v1/posts/user/" + readerUser.getId()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.content", hasSize(0)));
    }
  }

  @Nested
  @DisplayName("Get Posts by Category Tests")
  class GetPostsByCategoryTests {

    @Test
    @DisplayName("Should get posts by category ID")
    void getCategoryPosts_ValidCategory_ShouldReturnPosts() throws Exception {
      mockMvc
          .perform(get("/v1/posts/category/" + testCategory.getId()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(1))))
          .andExpect(jsonPath("$.data.content[0].title", notNullValue()));
    }

    @Test
    @DisplayName("Should return empty list for category with no posts")
    void getCategoryPosts_CategoryWithNoPosts_ShouldReturnEmpty() throws Exception {
      Category emptyCategory = new Category();
      emptyCategory.setName("Empty Category");
      emptyCategory.setSlug("empty-category");
      emptyCategory = categoryRepository.save(emptyCategory);

      mockMvc
          .perform(get("/v1/posts/category/" + emptyCategory.getId()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.content", hasSize(0)));
    }
  }
}
