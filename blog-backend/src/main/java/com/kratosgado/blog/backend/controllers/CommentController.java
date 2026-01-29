package com.kratosgado.blog.backend.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.kratosgado.blog.backend.annotations.OpenApi.DeleteEndpoint;
import com.kratosgado.blog.backend.annotations.OpenApi.GetEnpoint;
import com.kratosgado.blog.backend.annotations.OpenApi.SecuredUpdateEndpoint;
import com.kratosgado.blog.backend.security.SecurityUtils;
import com.kratosgado.blog.backend.services.CommentService;
import com.kratosgado.blog.dtos.request.CreateCommentRequest;
import com.kratosgado.blog.dtos.response.CommentResponse.CommentWithoutUser;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.models.Comment;
import com.kratosgado.blog.models.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/comments")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Comments", description = "Comment management APIs")
public class CommentController {

   private final CommentService commentService;

   public CommentController(CommentService commentService) {
     this.commentService = commentService;
   }

  @PostMapping
  @SecuredUpdateEndpoint
  @Operation(summary = "Create a new comment", description = "Creates a new comment on a post. Requires authentication.", security = @SecurityRequirement(name = "bearer-jwt"))
  @ResponseStatus(HttpStatus.CREATED)
  public Comment createComment(
      @Valid @RequestBody @Parameter(description = "Comment creation request") CreateCommentRequest request,
      @AuthenticationPrincipal User user) {
    return commentService.createComment(request, user);
  }

  @PutMapping("/{id}/approve")
  @SecuredUpdateEndpoint
  @Operation(summary = "Approve a comment", description = "Approves a pending comment. Requires authentication.", security = @SecurityRequirement(name = "bearer-jwt"))
  public Comment approveComment(@PathVariable @Parameter(description = "Comment ID") String id) {
    return commentService.approveComment(id);
  }

  @PutMapping("/{id}/reject")
  @SecuredUpdateEndpoint
  @Operation(summary = "Reject a comment", description = "Rejects a pending comment. Requires authentication.", security = @SecurityRequirement(name = "bearer-jwt"))
  public Comment rejectComment(@PathVariable @Parameter(description = "Comment ID") String id) {
    return commentService.rejectComment(id);
  }

  @GetMapping("/{id}")
  @GetEnpoint
  @Operation(summary = "Get a comment by ID", description = "Retrieves a single comment by its ID. Public access.")
  public Comment getComment(
      @PathVariable @Parameter(description = "Comment ID") String id) {
    return commentService.getCommentById(id);
  }

  @DeleteMapping("/{id}")
  @DeleteEndpoint
  @Operation(summary = "Delete a comment", description = "Deletes a comment by ID. Only the comment author can delete it.", security = @SecurityRequirement(name = "bearer-jwt"))
  public void deleteComment(
      @PathVariable @Parameter(description = "Comment ID") String id) {
    Long userId = SecurityUtils.getCurrentUserId();
    commentService.deleteComment(id, userId);
  }

  @GetMapping("/post/{postId}")
  @GetEnpoint
  @Operation(summary = "Get comments for a post", description = "Retrieves all approved comments for a specific post. Public access.")
   public PageResponse<Comment> getPostComments(
       @PathVariable @Parameter(description = "Post ID") Long postId,
       @RequestParam(value = "page", defaultValue = "0") int page,
       @RequestParam(value = "size", defaultValue = "20") int size) {
     return commentService.getPostComments(postId, page, size);
   }

  @GetMapping("/user/{userId}")
  @GetEnpoint
  @Operation(summary = "Get comments by user", description = "Retrieves all comments created by a specific user")
   public PageResponse<CommentWithoutUser> getUserComments(
       @PathVariable @Parameter(description = "User ID") Long userId,
       @RequestParam(value = "page", defaultValue = "0") int page,
       @RequestParam(value = "size", defaultValue = "20") int size) {
     return commentService.getUserComments(userId, page, size);
   }

  @GetMapping("/post/{postId}/count")
  @GetEnpoint
  @Operation(summary = "Get comment count for a post", description = "Returns the number of approved comments for a post")
  public Long getPostCommentCount(@PathVariable @Parameter(description = "Post ID") Long postId) {
    return commentService.getPostCommentCount(postId);
  }
}