package com.kratosgado.blog.backend.graphql;

import java.util.Collections;
import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import com.kratosgado.blog.backend.services.CommentService;
import com.kratosgado.blog.backend.services.PostService;
import com.kratosgado.blog.dtos.request.CreateCommentRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.PostResponse;
import com.kratosgado.blog.models.Comment;
import com.kratosgado.blog.models.User;

@Controller
public class CommentGraphQLController {

  private final CommentService commentService;
  private final PostService postService;

  public CommentGraphQLController(CommentService commentService, PostService postService) {
    this.commentService = commentService;
    this.postService = postService;
  }

  @QueryMapping
  public Comment comment(@Argument String id) {
    return commentService.getCommentById(id);
  }

  @QueryMapping
  public PageResponse<Comment> commentsByPost(
      @Argument Long postId,
      @Argument(name = "page") int page,
      @Argument(name = "size") int size) {
    return commentService.getPostComments(postId, page, size);

  }

  @MutationMapping
  public Comment createComment(@Argument CreateCommentRequest input, @AuthenticationPrincipal User user) {

    return commentService.createComment(input, user);
  }

  @MutationMapping
  public boolean deleteComment(@Argument String id, @AuthenticationPrincipal User user) {

    commentService.deleteComment(id, Long.valueOf(user.getId()));
    return true;
  }

  @MutationMapping
  public Comment updateComment(@Argument String id, @Argument String content) {
    throw new UnsupportedOperationException("Update comment not implemented");
  }

  @SchemaMapping(typeName = "Comment", field = "post")
  public PostResponse post(Comment comment) {
    return postService.getPostById(comment.getPostId());
  }

  @SchemaMapping(typeName = "Comment", field = "parent")
  public Comment parent(Comment comment) {
    return null;
  }

  @SchemaMapping(typeName = "Comment", field = "replies")
  public List<Comment> replies(Comment comment) {
    return Collections.emptyList();
  }
}
