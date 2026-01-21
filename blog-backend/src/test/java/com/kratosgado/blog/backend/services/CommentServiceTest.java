package com.kratosgado.blog.backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verifyNoInteractions;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.mongo.CommentRepository;
import com.kratosgado.blog.dtos.request.CreateCommentRequest;
import com.kratosgado.blog.enums.CommentStatus;
import com.kratosgado.blog.models.Comment;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService Tests")
class CommentServiceTest {

  @Mock
  private CommentRepository commentRepository;

  @InjectMocks
  private CommentService commentService;

  private Comment testComment;
  private CreateCommentRequest createRequest;
  private Pageable pageable;

  @BeforeEach
  void setUp() {
    testComment = new Comment(1L, 1L, "Test Comment");
    testComment.setId("comment123");
    testComment.setStatus(CommentStatus.approved);
    testComment.setCreatedAt(LocalDateTime.now());

    createRequest = new CreateCommentRequest(1L, "New Comment");
    pageable = PageRequest.of(0, 10);
  }

  @Test
  @DisplayName("Should successfully create a comment with pending status")
  void createComment_WithValidData_ShouldReturnPendingComment() {
    // Arrange
    when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

    // Act
    Comment result = commentService.createComment(createRequest, 1L);

    // Assert
    assertNotNull(result);
  }

  @ParameterizedTest
  @MethodSource("statusChangeTestCases")
  @DisplayName("Should successfully change comment status")
  void changeCommentStatus_WithValidId_ShouldUpdateStatus(CommentStatus newStatus, String method) throws Exception {
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
    assertEquals(newStatus, testComment.getStatus());
  }

  static Stream<Arguments> statusChangeTestCases() {
    return Stream.of(
        Arguments.of(CommentStatus.approved, "approve"),
        Arguments.of(CommentStatus.rejected, "reject")
    );
  }

  @ParameterizedTest
  @MethodSource("statusChangeNotFoundTestCases")
  @DisplayName("Should throw exception when approving or rejecting non-existent comment")
  void changeCommentStatus_WithNonExistentId_ShouldThrowException(String method) {
    // Arrange
    when(commentRepository.findById("comment123")).thenReturn(Optional.empty());

    // Act & Assert
    BlogException exception;
    if (method.equals("approve")) {
      exception = assertThrows(BlogException.class, 
          () -> commentService.approveComment("comment123"));
    } else {
      exception = assertThrows(BlogException.class, 
          () -> commentService.rejectComment("comment123"));
    }
    assertEquals("Comment not found", exception.getMessage());
  }

  static Stream<Arguments> statusChangeNotFoundTestCases() {
    return Stream.of(
        Arguments.of("approve"),
        Arguments.of("reject")
    );
  }

  @Test
  @DisplayName("Should successfully delete own comment")
  void deleteComment_AsOwner_ShouldDeleteComment() {
    // Arrange
    when(commentRepository.findById("comment123")).thenReturn(Optional.of(testComment));
    doNothing().when(commentRepository).delete(testComment);

    // Act
    commentService.deleteComment("comment123", 1L);

    // Assert - method completes without exception
  }

  @Test
  @DisplayName("Should throw exception when deleting someone else's comment")
  void deleteComment_AsNonOwner_ShouldThrowException() {
    // Arrange
    when(commentRepository.findById("comment123")).thenReturn(Optional.of(testComment));

    // Act
    BlogException exception = assertThrows(BlogException.class,
        () -> commentService.deleteComment("comment123", 2L));

    // Assert
    assertEquals("You are not allowed to delete this comment", exception.getMessage());
  }

  @Test
  @DisplayName("Should throw exception when deleting non-existent comment")
  void deleteComment_WithNonExistentId_ShouldThrowException() {
    // Arrange
    when(commentRepository.findById("comment123")).thenReturn(Optional.empty());

    // Act
    BlogException exception = assertThrows(BlogException.class,
        () -> commentService.deleteComment("comment123", 1L));

    // Assert
    assertEquals("Comment not found", exception.getMessage());
  }

  @Test
  @DisplayName("Should get only approved comments for a post")
  void getPostComments_ShouldReturnOnlyApprovedComments() {
    // Arrange
    Page<Comment> commentPage = new PageImpl<>(List.of(testComment));
    when(commentRepository.findByPostIdAndStatus(1L, CommentStatus.approved, pageable))
        .thenReturn(commentPage);

    // Act
    Page<Comment> result = commentService.getPostComments(1L, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
  }

  @Test
  @DisplayName("Should get all comments for a post regardless of status")
  void getAllPostComments_ShouldReturnAllComments() {
    // Arrange
    Page<Comment> commentPage = new PageImpl<>(List.of(testComment));
    when(commentRepository.findByPostId(1L, pageable)).thenReturn(commentPage);

    // Act
    Page<Comment> result = commentService.getAllPostComments(1L, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
  }

  @Test
  @DisplayName("Should get user comments")
  void getUserComments_WithUserId_ShouldReturnPageOfComments() {
    // Arrange
    Page<Comment> commentPage = new PageImpl<>(List.of(testComment));
    when(commentRepository.findByUserId(1L, pageable)).thenReturn(commentPage);

    // Act
    Page<Comment> result = commentService.getUserComments(1L, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
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
