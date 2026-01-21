package com.kratosgado.blog.backend.services;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.mongo.CommentRepository;
import com.kratosgado.blog.backend.utils.DtoMapper;
import com.kratosgado.blog.dtos.request.CreateCommentRequest;
import com.kratosgado.blog.dtos.response.CommentResponse;
import com.kratosgado.blog.enums.CommentStatus;
import com.kratosgado.blog.models.Comment;
import com.kratosgado.blog.models.User;

@Service
public class CommentService {

  private final CommentRepository commentRepository;
  private final UserService userService;

  public CommentService(CommentRepository commentRepository, UserService userService) {
    this.commentRepository = commentRepository;
    this.userService = userService;
  }

  @Transactional
  public CommentResponse createComment(CreateCommentRequest request, Long userId) {
    Comment comment = new Comment(request.postId(), userId, request.content());
    comment.setStatus(CommentStatus.pending);
    comment = commentRepository.save(comment);

    User user = userService.getUserById(userId);
    return DtoMapper.toCommentResponse(comment, user);
  }

  @Transactional
  public CommentResponse approveComment(String commentId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> BlogException.notFound("Comment not found"));

    comment.setStatus(CommentStatus.approved);
    comment = commentRepository.save(comment);

    User user = userService.getUserById(comment.getUserId());
    return DtoMapper.toCommentResponse(comment, user);
  }

  @Transactional
  public CommentResponse rejectComment(String commentId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> BlogException.notFound("Comment not found"));

    comment.setStatus(CommentStatus.rejected);
    comment = commentRepository.save(comment);

    User user = userService.getUserById(comment.getUserId());
    return DtoMapper.toCommentResponse(comment, user);
  }

  @Transactional
  public void deleteComment(String commentId, Long userId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> BlogException.notFound("Comment not found"));

    // Only the comment author can delete their comment
    if (!comment.getUserId().equals(userId)) {
      throw BlogException.unauthorized("You are not allowed to delete this comment");
    }

    commentRepository.delete(comment);

  }

  public Page<CommentResponse> getPostComments(Long postId, Pageable pageable) {
    Page<Comment> comments = commentRepository.findByPostIdAndStatus(postId, CommentStatus.approved, pageable);
    return enrichCommentsWithUserData(comments);
  }

  public Page<CommentResponse> getAllPostComments(Long postId, Pageable pageable) {
    Page<Comment> comments = commentRepository.findByPostId(postId, pageable);
    return enrichCommentsWithUserData(comments);
  }

  public Page<CommentResponse> getUserComments(Long userId, Pageable pageable) {
    Page<Comment> comments = commentRepository.findByUserId(userId, pageable);
    return enrichCommentsWithUserData(comments);
  }

  public Long getPostCommentCount(Long postId) {
    return commentRepository.countByPostIdAndStatus(postId, CommentStatus.approved);
  }

  /**
   * Enriches comments with user data using batch fetching to avoid N+1 queries.
   * Fetches all unique users in a single batch, then maps comments to responses.
   */
  private Page<CommentResponse> enrichCommentsWithUserData(Page<Comment> comments) {
    if (comments.isEmpty()) {
      return new PageImpl<>(List.of(), comments.getPageable(), 0);
    }

    // Extract unique userIds
    Set<Long> userIds = comments.getContent().stream()
        .map(Comment::getUserId)
        .collect(Collectors.toSet());

    // Batch fetch all users (1 query instead of N)
    Map<Long, User> userMap = userIds.stream()
        .map(id -> {
          try {
            return userService.getUserById(id);
          } catch (Exception e) {
            return null;
          }
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(User::getId, user -> user));

    // Map to responses
    List<CommentResponse> responses = comments.getContent().stream()
        .map(comment -> DtoMapper.toCommentResponse(
            comment,
            userMap.get(comment.getUserId())))
        .toList();

    return new PageImpl<>(responses, comments.getPageable(), comments.getTotalElements());
  }
}
