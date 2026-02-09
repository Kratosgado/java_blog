package com.kratosgado.blog.backend.graphql;

import com.kratosgado.blog.backend.security.SecurityUtils;
import com.kratosgado.blog.backend.services.CommentService;
import com.kratosgado.blog.backend.services.PostService;
import com.kratosgado.blog.backend.services.ReviewService;
import com.kratosgado.blog.backend.services.UserService;
import com.kratosgado.blog.dtos.request.CreatePostRequest;
import com.kratosgado.blog.dtos.request.PageRequest;
import com.kratosgado.blog.dtos.request.SearchPageRequest;
import com.kratosgado.blog.dtos.request.UpdatePostRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.PostResponse.PostDetails;
import com.kratosgado.blog.dtos.response.PostResponse.PostView;
import com.kratosgado.blog.dtos.response.PostResponse.PostWithoutCategory;
import com.kratosgado.blog.dtos.response.PostResponse.PostWithoutUser;
import com.kratosgado.blog.enums.PostStatus;
import com.kratosgado.blog.models.Comment;
import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.models.Review;
import com.kratosgado.blog.models.User;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class PostGraphQLController {

  private final PostService postService;
  private final UserService userService;
  private final CommentService commentService;
  private final ReviewService reviewService;

  public PostGraphQLController(
      PostService postService,
      UserService userService,
      CommentService commentService,
      ReviewService reviewService) {
    this.postService = postService;
    this.userService = userService;
    this.commentService = commentService;
    this.reviewService = reviewService;
  }

  @QueryMapping
  public PostDetails post(@Argument Long id) {
    return postService.getPostById(id);
  }

  @QueryMapping
  public PostDetails postBySlug(@Argument String slug) {
    return postService.getPostBySlug(slug);
  }

  @QueryMapping
  public PageResponse<PostView> posts(
      @Argument(name = "page") int page,
      @Argument(name = "size") int size,
      @Argument(name = "sortBy") String sortBy,
      @Argument(name = "sortDir") String sortDir) {
    String sortField = sortBy != null ? sortBy : "created_at";
    String direction = sortDir != null ? sortDir : "desc";
    return postService.getPublishedPosts(new PageRequest(page, size, sortField, direction));
  }

  @QueryMapping
  public PageResponse<PostView> searchPosts(
      @Argument String keyword, @Argument int page, @Argument int size) {
    var searchRequest = SearchPageRequest.builder().keyword(keyword).page(page).size(size).build();
    return postService.searchPosts(searchRequest);
  }

  @QueryMapping
  public PageResponse<PostWithoutCategory> postsByCategory(
      @Argument Long categoryId, @Argument int page, @Argument int size) {
    return postService.getPostsByCategory(
        categoryId, new PageRequest(page, size, "created_at", "desc"));
  }

  @QueryMapping
  public PageResponse<PostWithoutUser> postsByUser(
      @Argument Long userId, @Argument int page, @Argument int size) {
    return postService.getUserPosts(userId, new PageRequest(page, size, "created_at", "desc"));
  }

  // Mutations
  @MutationMapping
  public PostDetails createPost(@Argument CreatePostRequest input) {

    Long userId = SecurityUtils.getCurrentUserId();
    return postService.createPost(input, userService.getUserById(userId));
  }

  @MutationMapping
  public PostDetails updatePost(@Argument Long id, @Argument UpdatePostRequest input) {

    Long userId = SecurityUtils.getCurrentUserId();
    return postService.updatePost(id, input, userId);
  }

  @MutationMapping
  public boolean deletePost(@Argument Long id) {

    postService.deletePost(id, SecurityUtils.getCurrentUserId());
    return true;
  }

  @MutationMapping
  public PostDetails publishPost(@Argument Long id) {
    Long userId = SecurityUtils.getCurrentUserId();
    var post = postService.getPostById(id);
    UpdatePostRequest updateRequest =
        new UpdatePostRequest(
            post.getTitle(),
            post.getContent(),
            post.getExcerpt(),
            post.getCategory().getId(),
            post.getCoverImage(),
            PostStatus.published,
            null);
    return postService.updatePost(id, updateRequest, userId);
  }

  // Field resolvers for Post type
  @SchemaMapping(typeName = "Post", field = "slug")
  public String slug(Post post) {
    // Generate slug from title
    return post.getTitle().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
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
    if (PostStatus.published.equals(post.getStatus()) && post.getUpdatedAt() != null) {
      return post.getUpdatedAt().toString();
    }
    return null;
  }

  @SchemaMapping(typeName = "Post", field = "author")
  public User author(Post post) {
    return userService.getUserById(post.getUser().getId());
  }

  @SchemaMapping(typeName = "Post", field = "comments")
  public PageResponse<Comment> comments(Post post) {
    return commentService.getPostComments(post.getId(), new PageRequest(0, 100, "id", "desc"));
  }

  @SchemaMapping(typeName = "Post", field = "reviews")
  public PageResponse<Review> reviews(Post post) {
    return reviewService.getPostReviews(post.getId(), new PageRequest(0, 100, "id", "desc"));
  }
}
