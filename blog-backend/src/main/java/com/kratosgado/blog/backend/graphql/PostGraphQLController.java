package com.kratosgado.blog.backend.graphql;

import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.models.User;
import com.kratosgado.blog.models.Comment;
import com.kratosgado.blog.models.Review;
import com.kratosgado.blog.backend.services.PostService;
import com.kratosgado.blog.backend.services.UserService;
import com.kratosgado.blog.backend.services.CommentService;
import com.kratosgado.blog.backend.services.ReviewService;
import com.kratosgado.blog.dtos.request.CreatePostRequest;
import com.kratosgado.blog.dtos.request.UpdatePostRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  public Post post(@Argument Long id) {
    return postService.getPostById(id);
  }

  @QueryMapping
  public PageResponse<Post> posts(
      @Argument(name = "page") int page,
      @Argument(name = "size") int size,
      @Argument(name = "sortBy") String sortBy,
      @Argument(name = "sortDir") String sortDir) {
    Sort sort = sortDir.equalsIgnoreCase("asc")
        ? Sort.by(sortBy).ascending()
        : Sort.by(sortBy).descending();

    PageRequest pageRequest = PageRequest.of(page, size, sort);
    Page<Post> postsPage = postService.getPublishedPosts(pageRequest);

    return new PageResponse<>(
        postsPage.getContent(),
        postsPage.getNumber(),
        postsPage.getSize(),
        postsPage.getTotalElements(),
        postsPage.getTotalPages(),
        postsPage.isFirst(),
        postsPage.isLast()
    );
  }

  @QueryMapping
  public PageResponse<Post> searchPosts(
      @Argument String keyword,
      @Argument int page,
      @Argument int size) {
    PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<Post> postsPage = postService.searchPosts(keyword, pageRequest);

    return new PageResponse<>(
        postsPage.getContent(),
        postsPage.getNumber(),
        postsPage.getSize(),
        postsPage.getTotalElements(),
        postsPage.getTotalPages(),
        postsPage.isFirst(),
        postsPage.isLast()
    );
  }

  @QueryMapping
  public PageResponse<Post> postsByCategory(
      @Argument Long categoryId,
      @Argument int page,
      @Argument int size) {
    PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<Post> postsPage = postService.getPostsByCategory(categoryId, pageRequest);

    return new PageResponse<>(
        postsPage.getContent(),
        postsPage.getNumber(),
        postsPage.getSize(),
        postsPage.getTotalElements(),
        postsPage.getTotalPages(),
        postsPage.isFirst(),
        postsPage.isLast()
    );
  }

  @QueryMapping
  public PageResponse<Post> postsByUser(
      @Argument Long userId,
      @Argument int page,
      @Argument int size) {
    PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<Post> postsPage = postService.getUserPosts(userId, pageRequest);

    return new PageResponse<>(
        postsPage.getContent(),
        postsPage.getNumber(),
        postsPage.getSize(),
        postsPage.getTotalElements(),
        postsPage.getTotalPages(),
        postsPage.isFirst(),
        postsPage.isLast()
    );
  }

  // Mutations
  @MutationMapping
  public Post createPost(@Argument CreatePostRequest input) {
    // This would need userId from authentication context
    Long userId = 1L; // TODO: Get from SecurityContext
    return postService.createPost(input, userId);
  }

  @MutationMapping
  public Post updatePost(@Argument Long id, @Argument UpdatePostRequest input) {
    // This would need userId from authentication context
    Long userId = 1L; // TODO: Get from SecurityContext
    return postService.updatePost(id, input, userId);
  }

  @MutationMapping
  public boolean deletePost(@Argument Long id) {
    // This would need userId from authentication context
    Long userId = 1L; // TODO: Get from SecurityContext
    postService.deletePost(id, userId);
    return true;
  }

  @MutationMapping
  public Post publishPost(@Argument Long id) {
    // This would need userId from authentication context
    Long userId = 1L; // TODO: Get from SecurityContext
    Post post = postService.getPostById(id);
    UpdatePostRequest updateRequest = new UpdatePostRequest(
      post.getTitle(), post.getContent(), post.getExcerpt(), 
      post.getCategoryId(), post.getCoverImage(), "PUBLISHED"
    );
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
    return userService.getUserById(post.getUserId());
  }

  @SchemaMapping(typeName = "Post", field = "comments")
  public List<Comment> comments(Post post) {
    return commentService.getPostComments(post.getId(), PageRequest.of(0, 100)).getContent();
  }

  @SchemaMapping(typeName = "Post", field = "reviews")
  public List<Review> reviews(Post post) {
    return reviewService.getPostReviews(post.getId(), PageRequest.of(0, 100)).getContent();
  }

}
