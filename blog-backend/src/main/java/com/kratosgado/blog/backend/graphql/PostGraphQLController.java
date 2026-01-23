package com.kratosgado.blog.backend.graphql;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import com.kratosgado.blog.backend.security.SecurityUtils;
import com.kratosgado.blog.backend.services.CommentService;
import com.kratosgado.blog.backend.services.PostService;
import com.kratosgado.blog.backend.services.ReviewService;
import com.kratosgado.blog.backend.services.UserService;
import com.kratosgado.blog.dtos.request.CreatePostRequest;
import com.kratosgado.blog.dtos.request.UpdatePostRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.PostResponse;
import com.kratosgado.blog.enums.PostStatus;
import com.kratosgado.blog.models.Comment;
import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.models.Review;
import com.kratosgado.blog.models.User;

@Controller
public class PostGraphQLController {

  private final PostService postService;
  private final UserService userService;
  private final CommentService commentService;
  private final ReviewService reviewService;

  public PostGraphQLController(PostService postService, UserService userService,
      CommentService commentService, ReviewService reviewService) {
    this.postService = postService;
    this.userService = userService;
    this.commentService = commentService;
    this.reviewService = reviewService;
  }

  @QueryMapping
  public PostResponse post(@Argument Long id) {
    return postService.getPostById(id);
  }

  @QueryMapping
  public PostResponse postBySlug(@Argument String slug) {
    return postService.getPostBySlug(slug);
  }

  @QueryMapping
  public PageResponse<PostResponse> posts(
      @Argument(name = "page") int page,
      @Argument(name = "size") int size,
      @Argument(name = "sortBy") String sortBy,
      @Argument(name = "sortDir") String sortDir) {
    Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
    String sortField = sortBy != null ? sortBy : "createdAt";
    Pageable pageable = PageRequest.of(page - 1, size, Sort.by(direction, sortField));
    return postService.getPublishedPosts(pageable);
  }

  @QueryMapping
  public PageResponse<PostResponse> searchPosts(
      @Argument String keyword,
      @Argument int page,
      @Argument int size) {
    Pageable pageable = PageRequest.of(page - 1, size);
    return postService.searchPosts(keyword, pageable);
  }

  @QueryMapping
  public PageResponse<PostResponse> postsByCategory(
      @Argument Long categoryId,
      @Argument int page,
      @Argument int size) {
    Pageable pageable = PageRequest.of(page - 1, size);
    return postService.getPostsByCategory(categoryId, pageable);
  }

  @QueryMapping
  public PageResponse<PostResponse> postsByUser(
      @Argument Long userId,
      @Argument int page,
      @Argument int size) {
    Pageable pageable = PageRequest.of(page - 1, size);
    return postService.getUserPosts(userId, pageable);
  }

  // Mutations
  @MutationMapping
  public PostResponse createPost(@Argument CreatePostRequest input) {

    Long userId = SecurityUtils.getCurrentUserId();
    return postService.createPost(input, userService.getUserById(userId));
  }

  @MutationMapping
  public PostResponse updatePost(@Argument Long id, @Argument UpdatePostRequest input) {

    Long userId = SecurityUtils.getCurrentUserId();
    return postService.updatePost(id, input, userId);
  }

  @MutationMapping
  public boolean deletePost(@Argument Long id) {

    postService.deletePost(id, SecurityUtils.getCurrentUserId());
    return true;
  }

  @MutationMapping
  public PostResponse publishPost(@Argument Long id) {
    Long userId = SecurityUtils.getCurrentUserId();
    var post = postService.getPostById(id);
    UpdatePostRequest updateRequest = new UpdatePostRequest(
        post.title(), post.content(), post.excerpt(),
        post.category().id(), post.coverImage(), PostStatus.published);
    return postService.updatePost(id, updateRequest, userId);
  }

  // Field resolvers for Post type
  @SchemaMapping(typeName = "Post", field = "slug")
  public String slug(Post post) {
    // Generate slug from title
    return post.getTitle().toLowerCase()
        .replaceAll("[^a-z0-9]+", "-")
        .replaceAll("^-+|-+$", "");
  }

  @SchemaMapping(typeName = "Post", field = "featuredImage")
  public String featuredImage(Post post) {
    return post.getCoverImage();
  }

  @SchemaMapping(typeName = "Post", field = "viewCount")
  public Integer viewCount(Post post) {
    return post.getViews() != null ? post.getViews() : 0;
  }

  @SchemaMapping(typeName = "Post", field = "publishedAt")
  public String publishedAt(Post post) {
    if ("PUBLISHED".equals(post.getStatus()) && post.getUpdatedAt() != null) {
      return post.getUpdatedAt().toString();
    }
    return null;
  }

  @SchemaMapping(typeName = "Post", field = "author")
  public User author(Post post) {
    return userService.getUserById(Long.valueOf(post.getUserId()));
  }

  @SchemaMapping(typeName = "Post", field = "comments")
  public PageResponse<Comment> comments(Post post) {
    Pageable pageable = PageRequest.of(0, 100);
    return commentService.getPostComments(Long.valueOf(post.getId()), pageable);
  }

  @SchemaMapping(typeName = "Post", field = "reviews")
  public PageResponse<Review> reviews(Post post) {
    Pageable pageable = PageRequest.of(0, 100);
    return reviewService.getPostReviews(Long.valueOf(post.getId()), pageable);
  }

}
