package com.kratosgado.blog.backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.mongo.CommentRepository;
import com.kratosgado.blog.backend.repositories.jpa.PostRepository;
import com.kratosgado.blog.dtos.request.CreateCommentRequest;
import com.kratosgado.blog.dtos.request.PageRequest;
import com.kratosgado.blog.enums.CommentStatus;
import com.kratosgado.blog.models.Comment;
import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.models.User;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService Tests")
class CommentServiceTest {

  @Mock
  private CommentRepository commentRepository;

  @Mock
  private PostRepository postRepository;

  @InjectMocks
  private CommentService commentService;

  private Comment testComment;
  private User testUser;
  private Post testPost;
  private CreateCommentRequest createRequest;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testuser");
    testUser.setEmail("test@example.com");

    testPost = new Post();
    testPost.setId(1L);
    testPost.setTitle("Test Post");

    testComment = Comment.builder()
        .id("comment123")
        .postId(1L)
        .userId(1L)
        .content("Test Comment")
        .status(CommentStatus.approved)
        .createdAt(LocalDateTime.now())
        .authorName("testuser")
        .build();

    createRequest = new CreateCommentRequest(1L, "New Comment");
  }

  @Test
  @DisplayName("Should successfully create a comment with pending status")
  void createComment_WithValidData_ShouldReturnPendingComment() {
    // Arrange
    when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
    when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

    // Act
    Comment result = commentService.createComment(createRequest, testUser);

    // Assert
    assertNotNull(result);
    assertEquals(testUser.getUsername(), result.getAuthorName());
  }

  @ParameterizedTest
  @MethodSource("statusChangeTestCases")
  @DisplayName("Should successfully change comment status")
  void changeCommentStatus_WithValidId_ShouldUpdateStatus(CommentStatus newStatus, String method) {
    // Arrange
    testComment.setStatus(CommentStatus.pending);
    when(commentRepository.findById("comment123")).thenReturn(Optional.of(testComment));
    when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

    // Act
    Comment result;
    if (method.equals("approve")) {
      result = commentService.approveComment("comment123");
    } else {
      result = commentService.rejectComment("comment123");
    }

    // Assert
    assertNotNull(result);
    assertEquals(newStatus, result.getStatus());
  }

  static Stream<Arguments> statusChangeTestCases() {
    return Stream.of(
        Arguments.of(CommentStatus.approved, "approve"),
        Arguments.of(CommentStatus.rejected, "reject"));
  }

  @Test
  @DisplayName("Should get only approved comments for a post")
  void getPostComments_ShouldReturnOnlyApprovedComments() {
    // Arrange
    PageRequest pageRequest = PageRequest.builder().page(0).size(10).sortBy("createdAt").sortDir("DESC").build();
    Page<Comment> page = new PageImpl<>(List.of(testComment));
    when(commentRepository.findByPostIdAndStatus(eq(1L), eq(CommentStatus.approved), any(Pageable.class)))
        .thenReturn(page);

    // Act
    var result = commentService.getPostComments(1L, pageRequest);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.totalElements());
  }

  @Test
  @DisplayName("Should get approved comment count for a post")
  void getPostCommentCount_ShouldReturnCount() {
    // Arrange
    when(commentRepository.countByPostIdAndStatus(1L, CommentStatus.approved)).thenReturn(5L);

    // Act
    Long count = commentService.getPostCommentCount(1L);

    // Assert
    assertEquals(5L, count);
  }
}
