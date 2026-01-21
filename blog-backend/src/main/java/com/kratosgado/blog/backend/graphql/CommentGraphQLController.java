package com.kratosgado.blog.backend.graphql;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import com.kratosgado.blog.backend.services.CommentService;
import com.kratosgado.blog.backend.services.PostService;
import com.kratosgado.blog.backend.services.UserService;
import com.kratosgado.blog.dtos.request.CreateCommentRequest;
import com.kratosgado.blog.dtos.response.CommentResponse;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.PostResponse;
import com.kratosgado.blog.models.User;

@Controller
public class CommentGraphQLController {

  private final CommentService commentService;
  private final UserService userService;
  private final PostService postService;

  public CommentGraphQLController(CommentService commentService, UserService userService, PostService postService) {
    this.commentService = commentService;
    this.userService = userService;
    this.postService = postService;
  }

  @QueryMapping
  public CommentResponse comment(@Argument String id) {
    // Note: Comment uses String ID (MongoDB)
    return commentService.getPostComments(null, PageRequest.of(0, 1))
        .stream()
        .filter(c -> c.id().equals(id))
        .findFirst()
        .orElse(null);
  }

  @QueryMapping
  public PageResponse<CommentResponse> commentsByPost(
      @Argument Long postId,
      @Argument(name = "page") int page,
      @Argument(name = "size") int size) {
    PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<CommentResponse> commentsPage = commentService.getPostComments(postId, pageRequest);

    return new PageResponse<>(
        commentsPage.getContent(),
        commentsPage.getNumber(),
        commentsPage.getSize(),
        commentsPage.getTotalElements(),
        commentsPage.getTotalPages(),
        commentsPage.isFirst(),
        commentsPage.isLast());
  }

  @MutationMapping
  public CommentResponse createComment(@Argument CreateCommentRequest input) {

    Long userId = 1L; // TODO: Get from SecurityContext
    return commentService.createComment(input, userId);
  }

  @MutationMapping
  public boolean deleteComment(@Argument String id) {

    Long userId = 1L; // TODO: Get from SecurityContext
    commentService.deleteComment(id, userId);
    return true;
  }

  @MutationMapping
  public CommentResponse updateComment(@Argument String id, @Argument String content) {
    throw new UnsupportedOperationException("Update comment not implemented");
  }

  // Field resolvers for CommentResponse type
  @SchemaMapping(typeName = "Comment", field = "author")
  public User author(CommentResponse comment) {
    return userService.getUserById(comment.author().id());
  }

  @SchemaMapping(typeName = "Comment", field = "post")
  public PostResponse post(CommentResponse comment) {
    return postService.getPostById(comment.postId());
  }

  @SchemaMapping(typeName = "Comment", field = "parent")
  public CommentResponse parent(CommentResponse comment) {
    // Comment model doesn't have parentId field currently
    // Return null for now
    return null;
  }

  @SchemaMapping(typeName = "Comment", field = "replies")
  public List<CommentResponse> replies(CommentResponse comment) {
    // Comment model doesn't support replies currently
    // Return empty list for now
    return Collections.emptyList();
  }
}
