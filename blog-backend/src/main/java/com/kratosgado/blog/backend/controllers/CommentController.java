package com.kratosgado.blog.backend.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kratosgado.blog.backend.annotations.OpenApi.DeleteEndpoint;
import com.kratosgado.blog.backend.annotations.OpenApi.GetEnpoint;
import com.kratosgado.blog.backend.annotations.OpenApi.SecuredUpdateEndpoint;
import com.kratosgado.blog.backend.security.SecurityUtils;
import com.kratosgado.blog.backend.services.CommentService;
import com.kratosgado.blog.dtos.request.CreateCommentRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.models.Comment;
import com.kratosgado.blog.models.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

  private final CommentService commentService;

  @PostMapping
  @SecuredUpdateEndpoint
  public Comment createComment(
      @Valid @RequestBody CreateCommentRequest request, @AuthenticationPrincipal User user) {
    return commentService.createComment(request, user);

  }

  @PutMapping("/{id}/approve")
  @SecuredUpdateEndpoint
  public Comment approveComment(@PathVariable String id) {
    return commentService.approveComment(id);

  }

  @PutMapping("/{id}/reject")
  @SecuredUpdateEndpoint
  public Comment rejectComment(@PathVariable String id) {
    return commentService.rejectComment(id);

  }

  @GetMapping("/{id}")
  @GetEnpoint
  public Comment getComment(
      @PathVariable String id) {
    return commentService.getCommentById(id);
  }

  @DeleteMapping("/{id}")
  @DeleteEndpoint
  public void deleteComment(
      @PathVariable String id) {
    Long userId = SecurityUtils.getCurrentUserId();
    commentService.deleteComment(id, userId);
  }

  @GetMapping("/post/{postId}")
  @GetEnpoint
  public PageResponse<Comment> getPostComments(
      @PathVariable Long postId,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size) {
    return commentService.getPostComments(postId, page, size);

  }

  @GetMapping("/user/{userId}")
  @GetEnpoint
  public PageResponse<Comment> getUserComments(
      @PathVariable Long userId,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size) {
    return commentService.getUserComments(userId, page, size);
  }

  @GetMapping("/post/{postId}/count")
  @GetEnpoint
  public Long getPostCommentCount(@PathVariable Long postId) {
    return commentService.getPostCommentCount(postId);
  }
}
