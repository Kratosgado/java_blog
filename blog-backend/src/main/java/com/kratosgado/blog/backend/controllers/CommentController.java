package com.kratosgado.blog.backend.controllers;

import com.kratosgado.blog.dtos.request.CreateCommentRequest;
import com.kratosgado.blog.models.Comment;
import com.kratosgado.blog.backend.security.SecurityUtils;
import com.kratosgado.blog.backend.services.CommentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

  private static final Logger logger = LoggerFactory.getLogger(CommentController.class);
  
  private final CommentService commentService;

  @PostMapping
  public ResponseEntity<?> createComment(
    @Valid @RequestBody CreateCommentRequest request
  ) {
    try {
      Long userId = SecurityUtils.getCurrentUserId();
      Comment comment = commentService.createComment(request, userId);
      return ResponseEntity.ok(comment);
    } catch (Exception e) {
      logger.error("Failed to create comment", e);
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @PutMapping("/{id}/approve")
  public ResponseEntity<?> approveComment(@PathVariable Long id) {
    try {
      Comment comment = commentService.approveComment(id);
      return ResponseEntity.ok(comment);
    } catch (Exception e) {
      logger.error("Failed to approve comment {}", id, e);
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @PutMapping("/{id}/reject")
  public ResponseEntity<?> rejectComment(@PathVariable Long id) {
    try {
      Comment comment = commentService.rejectComment(id);
      return ResponseEntity.ok(comment);
    } catch (Exception e) {
      logger.error("Failed to reject comment {}", id, e);
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteComment(
    @PathVariable Long id
  ) {
    try {
      Long userId = SecurityUtils.getCurrentUserId();
      commentService.deleteComment(id, userId);
      return ResponseEntity.ok(Map.of("message", "Comment deleted successfully"));
    } catch (Exception e) {
      logger.error("Failed to delete comment {}", id, e);
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @GetMapping("/post/{postId}")
  public ResponseEntity<?> getPostComments(
    @PathVariable Long postId,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
  ) {
    try {
      PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").ascending());
      Page<Comment> comments = commentService.getPostComments(postId, pageRequest);
      
      return ResponseEntity.ok(Map.of(
        "content", comments.getContent(),
        "totalPages", comments.getTotalPages(),
        "totalElements", comments.getTotalElements(),
        "currentPage", comments.getNumber()
      ));
    } catch (Exception e) {
      logger.error("Failed to get post comments", e);
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<?> getUserComments(
    @PathVariable Long userId,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
  ) {
    try {
      PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
      Page<Comment> comments = commentService.getUserComments(userId, pageRequest);
      
      return ResponseEntity.ok(Map.of(
        "content", comments.getContent(),
        "totalPages", comments.getTotalPages(),
        "totalElements", comments.getTotalElements(),
        "currentPage", comments.getNumber()
      ));
    } catch (Exception e) {
      logger.error("Failed to get user comments", e);
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @GetMapping("/post/{postId}/count")
  public ResponseEntity<?> getPostCommentCount(@PathVariable Long postId) {
    try {
      Long count = commentService.getPostCommentCount(postId);
      return ResponseEntity.ok(Map.of("count", count));
    } catch (Exception e) {
      logger.error("Failed to get comment count", e);
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }
}
