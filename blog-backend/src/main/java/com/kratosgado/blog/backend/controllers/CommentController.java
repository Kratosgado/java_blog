package com.kratosgado.blog.backend.controllers;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kratosgado.blog.backend.annotations.OpenApi.DeleteEndpoint;
import com.kratosgado.blog.backend.annotations.OpenApi.GetEnpoint;
import com.kratosgado.blog.backend.annotations.OpenApi.SecuredUpdateEndpoint;
import com.kratosgado.blog.backend.security.SecurityUtils;
import com.kratosgado.blog.backend.services.CommentService;
import com.kratosgado.blog.dtos.request.CreateCommentRequest;
import com.kratosgado.blog.dtos.response.CommentResponse;
import com.kratosgado.blog.dtos.response.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

  private final CommentService commentService;

  @PostMapping
  @SecuredUpdateEndpoint
  public CommentResponse createComment(
      @Valid @RequestBody CreateCommentRequest request) {
    Long userId = SecurityUtils.getCurrentUserId();
    return commentService.createComment(request, userId);

  }

  @PutMapping("/{id}/approve")
  @SecuredUpdateEndpoint
  public CommentResponse approveComment(@PathVariable String id) {
    return commentService.approveComment(id);

  }

  @PutMapping("/{id}/reject")
  @SecuredUpdateEndpoint
  public CommentResponse rejectComment(@PathVariable String id) {
    return commentService.rejectComment(id);

  }

  @GetMapping("/{id}")
  @GetEnpoint
  public void getComment(
      @PathVariable String id) {
    Long userId = SecurityUtils.getCurrentUserId();
    commentService.deleteComment(id, userId);
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
  public PageResponse<CommentResponse> getPostComments(
      @PathVariable Long postId,
      @ParameterObject Pageable pageable) {
    Page<CommentResponse> comments = commentService.getPostComments(postId, pageable);

    return new PageResponse<CommentResponse>(comments.getContent(),
        pageable.getPageNumber() + 1,
        comments.getNumber(),
        comments.getTotalElements(),
        comments.getTotalPages(),
        comments.isFirst(),
        comments.isLast());
  }

  @GetMapping("/user/{userId}")
  @GetEnpoint
  public PageResponse<CommentResponse> getUserComments(
      @PathVariable Long userId,
      @ParameterObject org.springframework.data.domain.Pageable pageable) {
    Page<CommentResponse> comments = commentService.getUserComments(userId, pageable);

    return new PageResponse<CommentResponse>(comments.getContent(),
        pageable.getPageNumber() + 1,
        comments.getNumber(),
        comments.getTotalElements(),
        comments.getTotalPages(),
        comments.isFirst(),
        comments.isLast());
  }

  @GetMapping("/post/{postId}/count")
  @GetEnpoint
  public Long getPostCommentCount(@PathVariable Long postId) {
    return commentService.getPostCommentCount(postId);
  }
}
