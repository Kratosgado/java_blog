package com.kratosgado.blog.backend.integration;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.kratosgado.blog.dtos.request.CreateCommentRequest;
import com.kratosgado.blog.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests for Comment Controller endpoints. Tests comment creation, approval/rejection,
 * retrieval, deletion, and pagination. Uses MongoDB for comment storage.
 *
 * <p>Coverage:
 *
 * <ul>
 *   <li>Comment creation with authentication
 *   <li>Comment approval/rejection (AUTHOR/ADMIN only)
 *   <li>Get comment by ID
 *   <li>Get comments by post ID with pagination
 *   <li>Get comments by user ID with pagination
 *   <li>Comment count for posts
 *   <li>Delete comment (owner only)
 *   <li>Authorization checks
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Comment Controller Integration Tests")
public class CommentControllerIntegrationTest extends BaseIntegrationTest {

  private static final String COMMENTS_BASE_URL = "/v1/comments";
  
  // Test user constants
  private static final Long TEST_USER_ID = 1L;
  private static final String TEST_USER_EMAIL = "testuser@example.com";
  private static final Long TEST_AUTHOR_ID = 2L;
  private static final String TEST_AUTHOR_EMAIL = "author@example.com";
  private static final Long TEST_ADMIN_ID = 3L;
  private static final String TEST_ADMIN_EMAIL = "admin@example.com";

  @Nested
  @DisplayName("Create Comment Tests")
  class CreateCommentTests {

    @Test
    @DisplayName("Should create comment when authenticated")
    void createComment_WhenAuthenticated_ShouldReturn201() throws Exception {
      String token = generateToken(TEST_USER_ID, TEST_USER_EMAIL);
      CreateCommentRequest request = new CreateCommentRequest(1L, "Great article!");

      mockMvc
          .perform(
              post(COMMENTS_BASE_URL)
                  .header("Authorization", "Bearer " + token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.content").value("Great article!"))
          .andExpect(jsonPath("$.postId").value(1))
          .andExpect(jsonPath("$.userId").exists())
          .andExpect(jsonPath("$.status").value("pending"));
    }

    @Test
    @DisplayName("Should fail to create comment without authentication")
    void createComment_WhenNotAuthenticated_ShouldReturn401() throws Exception {
      CreateCommentRequest request = new CreateCommentRequest(1L, "Great article!");

      mockMvc
          .perform(
              post(COMMENTS_BASE_URL)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Should fail with blank content")
    void createComment_WithBlankContent_ShouldReturn400(String content) throws Exception {
      String token = generateToken(TEST_USER_ID, TEST_USER_EMAIL);
      CreateCommentRequest request = new CreateCommentRequest(1L, content);

      mockMvc
          .perform(
              post(COMMENTS_BASE_URL)
                  .header("Authorization", "Bearer " + token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
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
                  .header("Authorization", "Bearer " + token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(requestJson))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should fail with non-existent post ID")
    void createComment_WithNonExistentPostId_ShouldReturn404() throws Exception {
      String token = generateToken(TEST_USER_ID, TEST_USER_EMAIL);
      CreateCommentRequest request = new CreateCommentRequest(99999L, "Test comment");

      mockMvc
          .perform(
              post(COMMENTS_BASE_URL)
                  .header("Authorization", "Bearer " + token)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("Approve/Reject Comment Tests")
  class ApproveRejectCommentTests {

    @Test
    @DisplayName("Should approve comment when user is AUTHOR")
    void approveComment_WhenAuthor_ShouldReturn200() throws Exception {
      String token = generateToken(TEST_AUTHOR_ID, TEST_AUTHOR_EMAIL, UserRole.AUTHOR);
      String commentId = "test-comment-id";

      mockMvc
          .perform(
              put(COMMENTS_BASE_URL + "/" + commentId + "/approve")
                  .header("Authorization", "Bearer " + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.status").value("approved"));
    }

    @Test
    @DisplayName("Should approve comment when user is ADMIN")
    void approveComment_WhenAdmin_ShouldReturn200() throws Exception {
      String token = generateToken(TEST_ADMIN_ID, TEST_ADMIN_EMAIL, UserRole.ADMIN);
      String commentId = "test-comment-id";

      mockMvc
          .perform(
              put(COMMENTS_BASE_URL + "/" + commentId + "/approve")
                  .header("Authorization", "Bearer " + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.status").value("approved"));
    }

    @Test
    @DisplayName("Should fail to approve comment when user is READER")
    void approveComment_WhenReader_ShouldReturn403() throws Exception {
      String token = generateToken(TEST_USER_ID, TEST_USER_EMAIL); // Default role is READER
      String commentId = "test-comment-id";

      mockMvc
          .perform(
              put(COMMENTS_BASE_URL + "/" + commentId + "/approve")
                  .header("Authorization", "Bearer " + token))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should reject comment when user is AUTHOR")
    void rejectComment_WhenAuthor_ShouldReturn200() throws Exception {
      String token = generateToken(TEST_AUTHOR_ID, TEST_AUTHOR_EMAIL, UserRole.AUTHOR);
      String commentId = "test-comment-id";

      mockMvc
          .perform(
              put(COMMENTS_BASE_URL + "/" + commentId + "/reject")
                  .header("Authorization", "Bearer " + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.status").value("rejected"));
    }

    @Test
    @DisplayName("Should fail to approve non-existent comment")
    void approveComment_NonExistent_ShouldReturn404() throws Exception {
      String token = generateToken(TEST_ADMIN_ID, TEST_ADMIN_EMAIL, UserRole.ADMIN);
      String commentId = "non-existent-comment";

      mockMvc
          .perform(
              put(COMMENTS_BASE_URL + "/" + commentId + "/approve")
                  .header("Authorization", "Bearer " + token))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("Get Comment Tests")
  class GetCommentTests {

    @Test
    @DisplayName("Should get comment by ID without authentication")
    void getComment_ById_ShouldReturn200() throws Exception {
      String commentId = "existing-comment-id";

      mockMvc
          .perform(get(COMMENTS_BASE_URL + "/" + commentId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(commentId))
          .andExpect(jsonPath("$.content").exists());
    }

    @Test
    @DisplayName("Should fail to get non-existent comment")
    void getComment_NonExistent_ShouldReturn404() throws Exception {
      String commentId = "non-existent-comment";

      mockMvc
          .perform(get(COMMENTS_BASE_URL + "/" + commentId))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should get comments for post with pagination")
    void getPostComments_WithPagination_ShouldReturn200() throws Exception {
      Long postId = 1L;

      mockMvc
          .perform(
              get(COMMENTS_BASE_URL + "/post/" + postId)
                  .param("page", "0")
                  .param("size", "10")
                  .param("sortBy", "createdAt")
                  .param("sortDirection", "DESC"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content").isArray())
          .andExpect(jsonPath("$.totalElements").exists())
          .andExpect(jsonPath("$.totalPages").exists());
    }

    @ParameterizedTest
    @CsvSource({"0,5", "1,10", "0,20", "2,15"})
    @DisplayName("Should get post comments with various page sizes")
    void getPostComments_WithVariousPageSizes_ShouldReturn200(int page, int size)
        throws Exception {
      Long postId = 1L;

      mockMvc
          .perform(
              get(COMMENTS_BASE_URL + "/post/" + postId)
                  .param("page", String.valueOf(page))
                  .param("size", String.valueOf(size)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("Should get comments by user ID")
    void getUserComments_ShouldReturn200() throws Exception {
      Long userId = TEST_USER_ID;

      mockMvc
          .perform(
              get(COMMENTS_BASE_URL + "/user/" + userId)
                  .param("page", "0")
                  .param("size", "10"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content").isArray())
          .andExpect(jsonPath("$.totalElements").exists());
    }

    @Test
    @DisplayName("Should get comment count for post")
    void getPostCommentCount_ShouldReturn200() throws Exception {
      Long postId = 1L;

      mockMvc
          .perform(get(COMMENTS_BASE_URL + "/post/" + postId + "/count"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isNumber());
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
      CreateCommentRequest createRequest = new CreateCommentRequest(1L, "Test comment to delete");

      String response =
          mockMvc
              .perform(
                  post(COMMENTS_BASE_URL)
                      .header("Authorization", "Bearer " + token)
                      .contentType(MediaType.APPLICATION_JSON)
                      .content(objectMapper.writeValueAsString(createRequest)))
              .andExpect(status().isCreated())
              .andReturn()
              .getResponse()
              .getContentAsString();

      String commentId = objectMapper.readTree(response).get("id").asText();

      // Delete the comment
      mockMvc
          .perform(
              delete(COMMENTS_BASE_URL + "/" + commentId)
                  .header("Authorization", "Bearer " + token))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should fail to delete comment without authentication")
    void deleteComment_WithoutAuth_ShouldReturn401() throws Exception {
      String commentId = "test-comment-id";

      mockMvc.perform(delete(COMMENTS_BASE_URL + "/" + commentId)).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should fail to delete other user's comment")
    void deleteComment_OtherUsersComment_ShouldReturn403() throws Exception {
      String token = generateToken(TEST_USER_ID, TEST_USER_EMAIL);
      String commentId = "other-users-comment-id";

      mockMvc
          .perform(
              delete(COMMENTS_BASE_URL + "/" + commentId)
                  .header("Authorization", "Bearer " + token))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should fail to delete non-existent comment")
    void deleteComment_NonExistent_ShouldReturn404() throws Exception {
      String token = generateToken(TEST_USER_ID, TEST_USER_EMAIL);
      String commentId = "non-existent-comment";

      mockMvc
          .perform(
              delete(COMMENTS_BASE_URL + "/" + commentId)
                  .header("Authorization", "Bearer " + token))
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
      Long postId = 1L;

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
          .perform(get(COMMENTS_BASE_URL + "/post/" + postId).param("page", "0").param("size", "10"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content").isArray())
          .andExpect(jsonPath("$.content").isEmpty())
          .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("Should return 0 count for post with no comments")
    void getPostCommentCount_NoComments_ShouldReturn0() throws Exception {
      Long postId = 99999L;

      mockMvc
          .perform(get(COMMENTS_BASE_URL + "/post/" + postId + "/count"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").value(0));
    }

    @ParameterizedTest
    @ValueSource(ints = {50, 100, 200})
    @DisplayName("Should handle large page sizes (with max limit)")
    void getPostComments_WithLargePageSize_ShouldApplyMaxLimit(int size) throws Exception {
      Long postId = 1L;

      mockMvc
          .perform(
              get(COMMENTS_BASE_URL + "/post/" + postId)
                  .param("page", "0")
                  .param("size", String.valueOf(size)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content").isArray());
    }
  }

  @Nested
  @DisplayName("Authorization Tests")
  class AuthorizationTests {

    @Test
    @DisplayName("Should allow ADMIN to approve any comment")
    void admin_CanApproveAnyComment() throws Exception {
      String token = generateToken(TEST_ADMIN_ID, TEST_ADMIN_EMAIL, UserRole.ADMIN);
      String commentId = "any-comment-id";

      mockMvc
          .perform(
              put(COMMENTS_BASE_URL + "/" + commentId + "/approve")
                  .header("Authorization", "Bearer " + token))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should allow AUTHOR to reject any comment")
    void author_CanRejectAnyComment() throws Exception {
      String token = generateToken(TEST_AUTHOR_ID, TEST_AUTHOR_EMAIL, UserRole.AUTHOR);
      String commentId = "any-comment-id";

      mockMvc
          .perform(
              put(COMMENTS_BASE_URL + "/" + commentId + "/reject")
                  .header("Authorization", "Bearer " + token))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should prevent READER from approving comments")
    void reader_CannotApproveComments() throws Exception {
      String token = generateToken(TEST_USER_ID, TEST_USER_EMAIL);
      String commentId = "any-comment-id";

      mockMvc
          .perform(
              put(COMMENTS_BASE_URL + "/" + commentId + "/approve")
                  .header("Authorization", "Bearer " + token))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should prevent READER from rejecting comments")
    void reader_CannotRejectComments() throws Exception {
      String token = generateToken(TEST_USER_ID, TEST_USER_EMAIL);
      String commentId = "any-comment-id";

      mockMvc
          .perform(
              put(COMMENTS_BASE_URL + "/" + commentId + "/reject")
                  .header("Authorization", "Bearer " + token))
          .andExpect(status().isForbidden());
    }
  }
}
