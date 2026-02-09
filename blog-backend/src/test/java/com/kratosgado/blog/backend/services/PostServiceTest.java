package com.kratosgado.blog.backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.jpa.CategoryRepository;
import com.kratosgado.blog.backend.repositories.jpa.PostRepository;
import com.kratosgado.blog.backend.repositories.jpa.TagRepository;
import com.kratosgado.blog.backend.repositories.jpa.UserRepository;
import com.kratosgado.blog.dtos.request.PageRequest;
import com.kratosgado.blog.dtos.request.SearchPageRequest;
import com.kratosgado.blog.dtos.request.UpdatePostRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.PostResponse.PostDetails;
import com.kratosgado.blog.dtos.response.PostResponse.PostView;
import com.kratosgado.blog.dtos.response.PostResponse.PostWithoutCategory;
import com.kratosgado.blog.dtos.response.PostResponse.PostWithoutUser;
import com.kratosgado.blog.enums.PostStatus;
import com.kratosgado.blog.models.Category;
import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.models.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostService Tests")
class PostServiceTest {

  @Mock private PostRepository postRepository;

  @Mock private TagRepository tagRepository;

  @Mock private UserRepository userRepository;

  @Mock private CategoryRepository categoryRepository;

  @InjectMocks private PostService postService;

  private Post testPost;
  private User testUser;
  private Category testCategory;
  private UpdatePostRequest updateRequest;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testuser");
    testUser.setEmail("test@example.com");
    testUser.setAvatarUrl("avatar.jpg");

    testCategory = new Category();
    testCategory.setId(1L);
    testCategory.setName("Test Category");
    testCategory.setSlug("test-category");

    testPost = new Post();
    testPost.setId(1L);
    testPost.setUser(testUser);
    testPost.setTitle("Test Post");
    testPost.setContent("Test Content");
    testPost.setExcerpt("Test Excerpt");
    testPost.setStatus(PostStatus.published);
    testPost.setCategory(testCategory);
    testPost.setCreatedAt(LocalDateTime.now());
    testPost.setSlug("test-post");

    updateRequest =
        new UpdatePostRequest(
            "Updated Title",
            "Updated Content",
            "Updated Excerpt",
            1L,
            "new-cover.jpg",
            PostStatus.published,
            new Long[] {3L});
  }

  @Test
  @DisplayName("Should throw exception when post not found for update")
  void postNotFound_ShouldThrowException_forUpdate() {
    // Arrange
    when(postRepository.findById(eq(1L))).thenReturn(Optional.empty());

    // Act & Assert
    BlogException exception =
        assertThrows(BlogException.class, () -> postService.updatePost(1L, updateRequest, 1L));
    assertEquals("Post not found", exception.getMessage());
  }

  @Test
  @DisplayName("Should successfully get post by slug")
  void getPostBySlug_WithValidSlug_ShouldReturnPostResponse() {
    // Arrange
    String slug = "test-post";
    PostDetails mockPostDetails = org.mockito.Mockito.mock(PostDetails.class);
    when(mockPostDetails.getId()).thenReturn(testPost.getId());
    when(mockPostDetails.getSlug()).thenReturn(testPost.getSlug());
    when(postRepository.findBySlug(slug)).thenReturn(Optional.of(mockPostDetails));

    // Act
    PostDetails result = postService.getPostBySlug(slug);

    // Assert
    assertNotNull(result);
    assertEquals(testPost.getId(), result.getId());
    assertEquals(testPost.getSlug(), result.getSlug());
    verify(postRepository).findBySlug(slug);
  }

  @Test
  @DisplayName("Should successfully get post by ID from database")
  void getPostById_WithValidId_ShouldReturnPostResponseFromDatabase() {
    // Arrange
    PostDetails mockPostDetails = org.mockito.Mockito.mock(PostDetails.class);
    when(mockPostDetails.getId()).thenReturn(testPost.getId());
    when(postRepository.findPostDetailsById(eq(1L))).thenReturn(Optional.of(mockPostDetails));

    // Act
    PostDetails result = postService.getPostById(1L);

    // Assert
    assertNotNull(result);
    assertEquals(testPost.getId(), result.getId());
    verify(postRepository).findPostDetailsById(eq(1L));
  }

  @Test
  @DisplayName("Should successfully get published posts")
  void getPublishedPosts_ShouldReturnPageOfPosts() {
    // Arrange
    PageRequest pageRequest =
        com.kratosgado.blog.dtos.request.PageRequest.builder()
            .page(0)
            .size(10)
            .sortBy("createdAt")
            .sortDir("DESC")
            .build();
    PostView mockPostView = org.mockito.Mockito.mock(PostView.class);
    Page<PostView> page = new PageImpl<>(List.of(mockPostView));
    when(postRepository.findByStatus(eq(PostStatus.published), any(Pageable.class)))
        .thenReturn(page);

    // Act
    PageResponse<PostView> result = postService.getPublishedPosts(pageRequest);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.totalElements());
  }

  @Test
  @DisplayName("Should successfully search posts")
  void searchPosts_WithKeyword_ShouldReturnPageOfPosts() {
    // Arrange
    String keyword = "test";
    SearchPageRequest pageRequest =
        SearchPageRequest.builder()
            .page(0)
            .size(10)
            .sortBy("createdAt")
            .sortDir("DESC")
            .keyword(keyword)
            .build();
    PostView mockPostView = org.mockito.Mockito.mock(PostView.class);
    Page<PostView> page = new PageImpl<>(List.of(mockPostView));
    // Service adds wildcards: wildcardQuery = "%" + keyword + "%"
    when(postRepository.searchPublishedPosts(eq(keyword), any(Pageable.class))).thenReturn(page);

    // Act
    PageResponse<PostView> result = postService.searchPosts(pageRequest);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.totalElements());
  }

  @Test
  @DisplayName("Should successfully get user posts")
  void getUserPosts_WithUserId_ShouldReturnPageOfPostResponses() {
    // Arrange
    com.kratosgado.blog.dtos.request.PageRequest pageRequest =
        com.kratosgado.blog.dtos.request.PageRequest.builder()
            .page(0)
            .size(10)
            .sortBy("createdAt")
            .sortDir("DESC")
            .build();
    PostWithoutUser mockPostWithoutUser = org.mockito.Mockito.mock(PostWithoutUser.class);
    Page<PostWithoutUser> page = new PageImpl<>(List.of(mockPostWithoutUser));
    when(postRepository.findByUserId(eq(1L), any(Pageable.class))).thenReturn(page);

    // Act
    PageResponse<PostWithoutUser> result = postService.getUserPosts(1L, pageRequest);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.totalElements());
    assertEquals(1, result.content().size());
  }

  @Test
  @DisplayName("Should successfully get posts by category")
  void getPostsByCategory_WithCategoryId_ShouldReturnPageOfPostResponses() {
    // Arrange
    PageRequest pageRequest =
        PageRequest.builder().page(0).size(10).sortBy("createdAt").sortDir("DESC").build();
    PostWithoutCategory mockPostWithoutCategory =
        org.mockito.Mockito.mock(PostWithoutCategory.class);
    Page<PostWithoutCategory> page = new PageImpl<>(List.of(mockPostWithoutCategory));
    when(postRepository.findByCategoryId(eq(1L), any(Pageable.class))).thenReturn(page);

    // Act
    PageResponse<PostWithoutCategory> result = postService.getPostsByCategory(1L, pageRequest);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.totalElements());
    assertEquals(1, result.content().size());
  }

  @Test
  @DisplayName("Should successfully get trending posts")
  void getTrendingPosts_ShouldReturnPageOfPosts() {
    // Arrange
    PageRequest pageRequest = PageRequest.builder().page(0).size(10).build();
    PostView mockPostView = org.mockito.Mockito.mock(PostView.class);
    Page<PostView> page = new PageImpl<>(List.of(mockPostView));

    when(postRepository.findTrendingPosts(
            eq(PostStatus.published), any(LocalDateTime.class), any(Pageable.class)))
        .thenReturn(page);

    // Act
    PageResponse<PostView> result = postService.getTrendingPosts(pageRequest);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.totalElements());
  }

  @Test
  @DisplayName("Should successfully get optimized category posts")
  void getCategoryPostsOptimized_ShouldReturnPageOfPosts() {
    // Arrange
    PageRequest pageRequest = PageRequest.builder().page(0).size(10).build();
    PostView mockPostView = org.mockito.Mockito.mock(PostView.class);
    Page<PostView> page = new PageImpl<>(List.of(mockPostView));

    when(postRepository.findPublishedPostsByCategoryOptimized(eq(1L), any(Pageable.class)))
        .thenReturn(page);

    // Act
    PageResponse<PostView> result =
        postService.getPublishedPostsByCategoryOptimized(1L, pageRequest);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.totalElements());
  }

  @Test
  @DisplayName("Should successfully get optimized tag posts")
  void getTagPostsOptimized_ShouldReturnPageOfPosts() {
    // Arrange
    PageRequest pageRequest = PageRequest.builder().page(0).size(10).build();
    PostView mockPostView = org.mockito.Mockito.mock(PostView.class);
    Page<PostView> page = new PageImpl<>(List.of(mockPostView));

    when(postRepository.findPublishedPostsByTagOptimized(eq(1L), any(Pageable.class)))
        .thenReturn(page);

    // Act
    PageResponse<PostView> result = postService.getPublishedPostsByTagOptimized(1L, pageRequest);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.totalElements());
  }
}
