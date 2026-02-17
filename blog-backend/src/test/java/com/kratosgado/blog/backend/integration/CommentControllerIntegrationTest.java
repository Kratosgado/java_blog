package com.kratosgado.blog.backend.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.kratosgado.blog.backend.repositories.jpa.PostRepository;
import com.kratosgado.blog.backend.repositories.jpa.UserRepository;
import com.kratosgado.blog.backend.repositories.mongo.CommentRepository;
import com.kratosgado.blog.dtos.request.CreateCommentRequest;
import com.kratosgado.blog.enums.CommentStatus;
import com.kratosgado.blog.enums.PostStatus;
import com.kratosgado.blog.enums.UserRole;
import com.kratosgado.blog.backend.models.Comment;
import com.kratosgado.blog.backend.models.Post;
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

@DisplayName("Comment Controller Integration Tests")
public class CommentControllerIntegrationTest extends BaseIntegrationTest {

  private static final String COMMENTS_BASE_URL = "/v1/comments";

  // Test user constants
  private static Long TEST_USER_ID = 1L;
  private static final String TEST_USER_EMAIL = "testuser@example.com";
  private static Long TEST_ADMIN_ID;
  private static final String TEST_ADMIN_EMAIL = "admin@example.com";
  private static Long postId;
  private static String commentId;

  private User testUser;
  private User adminUser;

  @Autowired private UserRepository userRepository;
  @Autowired private PostRepository postRepository;
  @Autowired private CommentRepository commentRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @BeforeEach
  @Override
  void baseSetUp() {
    // Clean up database
    userRepository.deleteAll();
    // Create admin user (ADMIN role)
    adminUser =
        User.builder()
            .email(TEST_ADMIN_EMAIL)
            .username("adminuser")
            .password(passwordEncoder.encode("@Password123"))
            .role(UserRole.ADMIN)
            .build();
    adminUser = userRepository.save(adminUser);
    TEST_ADMIN_ID = adminUser.getId();

    testUser =
        User.builder()
            .email(TEST_USER_EMAIL)
            .username("testuser")
            .password(passwordEncoder.encode("@Password123"))
            .role(UserRole.READER)
            .build();
    testUser = userRepository.save(testUser);
    TEST_USER_ID = testUser.getId();

    // Create test post
    Post testPost =
        Post.builder()
            .title("Test Post")
            .slug("test-post")
            .id(postId)
            .content("This is test post content")
            .excerpt("Test excerpt")
            .status(PostStatus.published)
            .user(adminUser)
            .build();
    testPost = postRepository.save(testPost);
    postId = testPost.getId();
    var comment =
        Comment.builder()
            .authorName(testUser.getUsername())
            .authorAvatarUrl(testUser.getAvatarUrl())
            .content("Great article!")
            .postId(postId)
            .status(CommentStatus.pending)
            .userId(testUser.getId())
            .build();

    commentRepository.save(comment);
    commentId = comment.getId();
  }

  @Nested
  @DisplayName("Create Comment Tests")
  class CreateCommentTests {

    @Test
    @DisplayName("Should fail to create comment without authentication")
    void createComment_WhenNotAuthenticated_ShouldReturn401() throws Exception {
      CreateCommentRequest request = new CreateCommentRequest(postId, "Great article!");

      mockMvc
          .perform(
              post(COMMENTS_BASE_URL)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Should fail with blank content")
    void createComment_WithBlankContent_ShouldReturn400(String content) throws Exception {
      String token = generateToken(TEST_USER_ID, TEST_USER_EMAIL);
      CreateCommentRequest request = new CreateCommentRequest(postId, content);

      mockMvc
          .perform(
              post(COMMENTS_BASE_URL)
                  .header("Authorization", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should fail with null post ID")
    void createComment_WithNullPostId_ShouldReturn400() throws Exception {
      String token = generateToken(TEST_USER_ID, TEST_USER_EMAIL);
      String requestJson = "{\"postId\": null, \"content\": \"Test comment\"}";

      mockMvc
          .perform(
              post(COMMENTS_BASE_URL)
                  .header("Authorization", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(requestJson))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should fail with non-existent post ID")
    void createComment_WithNonExistentPostId_ShouldReturn404() throws Exception {
      String token = generateToken(TEST_ADMIN_ID, TEST_ADMIN_EMAIL);
      CreateCommentRequest request = new CreateCommentRequest(99999L, "Test comment");

      mockMvc
          .perform(
              post(COMMENTS_BASE_URL)
                  .header("Authorization", token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(toJson(request)))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("Approve/Reject Comment Tests")
  class ApproveRejectCommentTests {

    @Test
    @DisplayName("Should approve comment when user is ADMIN")
    void approveComment_WhenAdmin_ShouldReturn200() throws Exception {
      String token = generateToken(TEST_ADMIN_ID, TEST_ADMIN_EMAIL, UserRole.ADMIN);
      mockMvc
          .perform(
              put(COMMENTS_BASE_URL + "/" + commentId + "/approve").header("Authorization", token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.status").value("approved"));
    }

    @Test
    @DisplayName("Should fail to approve comment when user is READER")
    void approveComment_WhenReader_ShouldReturn403() throws Exception {
      String token = generateToken(TEST_USER_ID, TEST_USER_EMAIL); // Default role is READER

      mockMvc
          .perform(
              put(COMMENTS_BASE_URL + "/" + commentId + "/approve").header("Authorization", token))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should fail to approve non-existent comment")
    void approveComment_NonExistent_ShouldReturn404() throws Exception {
      String token = generateToken(TEST_ADMIN_ID, TEST_ADMIN_EMAIL, UserRole.ADMIN);
      String commentId = "non-existent-comment-id";

      mockMvc
          .perform(
              put(COMMENTS_BASE_URL + "/" + commentId + "/approve").header("Authorization", token))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("Get Comment Tests")
  class GetCommentTests {

    @Test
    @DisplayName("Should get comment by ID without authentication")
    void getComment_ById_ShouldReturn200() throws Exception {

      mockMvc
          .perform(get(COMMENTS_BASE_URL + "/" + commentId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.id").value(commentId))
          .andExpect(jsonPath("$.data.content").exists());
    }

    @Test
    @DisplayName("Should fail to get non-existent comment")
    void getComment_NonExistent_ShouldReturn404() throws Exception {
      String commentId = "non-existent-comment";

      mockMvc.perform(get(COMMENTS_BASE_URL + "/" + commentId)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should get comments for post with pagination")
    void getPostComments_WithPagination_ShouldReturn200() throws Exception {

      mockMvc
          .perform(
              get(COMMENTS_BASE_URL + "/post/" + postId)
                  .param("page", "0")
                  .param("size", "10")
                  .param("sortBy", "createdAt")
                  .param("sortDirection", "DESC"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.content").isArray())
          .andExpect(jsonPath("$.data.totalElements").exists())
          .andExpect(jsonPath("$.data.totalPages").exists());
    }

    @ParameterizedTest
    @CsvSource({"0,5", "1,10", "0,20", "2,15"})
    @DisplayName("Should get post comments with various page sizes")
    void getPostComments_WithVariousPageSizes_ShouldReturn200(int page, int size) throws Exception {

      mockMvc
          .perform(
              get(COMMENTS_BASE_URL + "/post/" + postId)
                  .param("page", String.valueOf(page))
                  .param("size", String.valueOf(size)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("Should get comments by user ID")
    void getUserComments_ShouldReturn200() throws Exception {
      Long userId = TEST_USER_ID;

      mockMvc
          .perform(
              get(COMMENTS_BASE_URL + "/user/" + userId).param("page", "0").param("size", "10"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.content").isArray())
          .andExpect(jsonPath("$.data.totalElements").exists());
    }
  }

  @Nested
  @DisplayName("Delete Comment Tests")
  class DeleteCommentTests {

    @Test
    @DisplayName("Should delete own comment")
    void deleteComment_WhenOwner_ShouldReturn200() throws Exception {
      // Create a comment first
      String token = generateToken(TEST_USER_ID, TEST_USER_EMAIL);
      // Delete the comment
      mockMvc
          .perform(delete(COMMENTS_BASE_URL + "/" + commentId).header("Authorization", token))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should fail to delete comment without authentication")
    void deleteComment_WithoutAuth_ShouldReturn401() throws Exception {
      String commentId = "test-comment-id";

      mockMvc
          .perform(delete(COMMENTS_BASE_URL + "/" + commentId))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should fail to delete non-existent comment")
    void deleteComment_NonExistent_ShouldReturn404() throws Exception {
      String token = generateToken(TEST_ADMIN_ID, TEST_ADMIN_EMAIL);
      String commentId = "non-existent-comment";

      mockMvc
          .perform(delete(COMMENTS_BASE_URL + "/" + commentId).header("Authorization", token))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("Pagination Edge Cases")
  class PaginationEdgeCaseTests {

    @ParameterizedTest
    @CsvSource({"-1,10", "0,-5", "-1,-5", "0,0"})
    @DisplayName("Should handle invalid pagination parameters")
    void getPostComments_WithInvalidPagination_ShouldReturn400(int page, int size)
        throws Exception {

      mockMvc
          .perform(
              get(COMMENTS_BASE_URL + "/post/" + postId)
                  .param("page", String.valueOf(page))
                  .param("size", String.valueOf(size)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return empty list for post with no comments")
    void getPostComments_NoComments_ShouldReturnEmptyList() throws Exception {
      Long postId = 99999L; // Non-existent post

      mockMvc
          .perform(
              get(COMMENTS_BASE_URL + "/post/" + postId).param("page", "0").param("size", "10"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.content").isArray())
          .andExpect(jsonPath("$.data.content").isEmpty())
          .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    @DisplayName("Should return 0 count for post with no comments")
    void getPostCommentCount_NoComments_ShouldReturn0() throws Exception {
      Long postId = 99999L;

      mockMvc
          .perform(get(COMMENTS_BASE_URL + "/post/" + postId + "/count"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data").value(0));
    }

    @ParameterizedTest
    @ValueSource(ints = {50, 100, 200})
    @DisplayName("Should handle large page sizes (with max limit)")
    void getPostComments_WithLargePageSize_ShouldApplyMaxLimit(int size) throws Exception {

      mockMvc
          .perform(
              get(COMMENTS_BASE_URL + "/post/" + postId)
                  .param("page", "0")
                  .param("size", String.valueOf(size)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.content").isArray());
    }
  }
}
