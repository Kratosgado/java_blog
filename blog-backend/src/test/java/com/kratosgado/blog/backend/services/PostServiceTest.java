package com.kratosgado.blog.backend.services;

import com.kratosgado.blog.backend.cache.CacheConfig.PostCache;
import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.jdbc.CategoryRepository;
import com.kratosgado.blog.backend.repositories.jdbc.PostRepository;
import com.kratosgado.blog.backend.repositories.jdbc.TagRepository;
import com.kratosgado.blog.backend.repositories.jdbc.UserRepository;
import com.kratosgado.blog.backend.utils.DtoMapper;
import com.kratosgado.blog.dtos.request.CreatePostRequest;
import com.kratosgado.blog.dtos.request.PageRequest;
import com.kratosgado.blog.dtos.request.UpdatePostRequest;
import com.kratosgado.blog.dtos.response.*;
import com.kratosgado.blog.enums.PostStatus;
import com.kratosgado.blog.models.Category;
import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostService Tests")
class PostServiceTest {

  @Mock
  private PostRepository postRepository;

  @Mock
  private TagRepository tagRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private PostCache postCache;

  @InjectMocks
  private PostService postService;

  private Post testPost;
  private User testUser;
  private Category testCategory;
  private PostResponse testPostResponse;
  private CreatePostRequest createRequest;
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
    testPost.setUserId(1L);
    testPost.setTitle("Test Post");
    testPost.setContent("Test Content");
    testPost.setExcerpt("Test Excerpt");
    testPost.setStatus(PostStatus.published);
    testPost.setCategoryId(1L);
    testPost.setCreatedAt(LocalDateTime.now());

    // Create test PostResponse
    testPostResponse = new PostResponse(
        1L,
        new AuthorSummary(1L, "testuser", "test@example.com", "avatar.jpg"),
        new CategorySummary(1L, "Test Category", "test-category"),
        "test-post", // slug
        "Test Post",
        "Test Content",
        "Test Excerpt",
        PostStatus.published,
        LocalDateTime.now(),
        LocalDateTime.now(),
        0,
        0,
        null,
        List.of());

    createRequest = new CreatePostRequest(
        "New Post",
        "New Content",
        "New Excerpt",
        1L,
        "cover.jpg",
        "draft",
        new Long[] { 1L, 2L });

    updateRequest = new UpdatePostRequest(
        "Updated Title",
        "Updated Content",
        "Updated Excerpt",
        2L,
        "new-cover.jpg",
        PostStatus.published,
        new Long[] { 3L });
  }

  // @Test
  // @DisplayName("Should successfully create a post")
  // void createPost_WithValidData_ShouldReturnPostResponse() {
  // // Arrange
  // when(postRepository.save(any(Post.class))).thenReturn(testPost);
  // when(categoryRepository.existsById(anyLong())).thenReturn(true);

  // try (MockedStatic<DtoMapper> dtoMapperMock = mockStatic(DtoMapper.class)) {
  // dtoMapperMock
  // .when(() -> DtoMapper.toPostResponse(any(Post.class)))
  // .thenReturn(testPostResponse);

  // // Act
  // PostResponse result = postService.createPost(createRequest, testUser);

  // // Assert
  // assertNotNull(result);
  // assertEquals(testPostResponse.id(), result.id());
  // assertEquals(testPostResponse.title(), result.title());
  // }
  // }

  // @Test
  // @DisplayName("Should successfully update a post")
  // void updatePost_WithValidData_ShouldReturnUpdatedPostResponse() {
  // // Arrange
  // when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
  // when(postRepository.save(any(Post.class))).thenReturn(testPost);

  // PostResponse updatedResponse = new PostResponse(
  // 1L,
  // testPostResponse.author(),
  // testPostResponse.category(),
  // testPostResponse.slug(),
  // updateRequest.title(),
  // updateRequest.content(),
  // updateRequest.excerpt(),
  // updateRequest.status(),
  // testPostResponse.createdAt(),
  // LocalDateTime.now(),
  // 0,
  // 0,
  // updateRequest.coverImage(),
  // testPostResponse.tags());

  // try (MockedStatic<DtoMapper> dtoMapperMock = mockStatic(DtoMapper.class)) {
  // dtoMapperMock
  // .when(() -> DtoMapper.toPostResponse(any(Post.class)))
  // .thenReturn(updatedResponse);

  // // Act
  // PostResponse result = postService.updatePost(1L, updateRequest, 1L);

  // // Assert
  // assertNotNull(result);
  // assertEquals(updateRequest.title(), result.title());
  // assertEquals(updateRequest.content(), result.content());
  // }
  // }

  @Test
  @DisplayName("Should throw exception when post not found for update")
  void postNotFound_ShouldThrowException_forUpdate() throws SQLException {
    // Arrange
    when(postRepository.findById(eq(1L))).thenReturn(Optional.empty());

    // Act & Assert
    BlogException exception = assertThrows(BlogException.class,
        () -> postService.updatePost(1L, updateRequest, 1L));
    assertEquals("Post not found", exception.getMessage());
  }

  static Stream<Arguments> postNotFoundTestCases() {
    return Stream.of(
        Arguments.of("update"),
        Arguments.of("delete"),
        Arguments.of("getById"));
  }

  // @Test
  // @DisplayName("Should successfully delete a post")
  // void deletePost_WithValidId_ShouldDeletePost() {
  // // Arrange
  // when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
  // doNothing().when(postRepository).deleteById(1L);

  // // Act
  // postService.deletePost(1L, 1L);

  // // Assert
  // verify(postRepository).deleteById(1L);
  // }

  @Test
  @DisplayName("Should successfully get post by slug from cache")
  void getPostBySlug_WithValidSlug_ShouldReturnPostResponseFromCache() throws SQLException {
    // Arrange
    String slug = "test-post";
    when(postCache.get(slug)).thenReturn(Optional.of(testPostResponse));

    // Act
    PostResponse result = postService.getPostBySlug(slug);

    // Assert
    assertNotNull(result);
    assertEquals(testPostResponse.id(), result.id());
    assertEquals(testPostResponse.slug(), result.slug());
    assertEquals(testPostResponse.title(), result.title());
    verify(postRepository, never()).findBySlug(slug);
  }

  @Test
  @DisplayName("Should successfully get post by slug from database on cache miss")
  void getPostBySlug_OnCacheMiss_ShouldFetchFromDatabase() throws SQLException {
    // Arrange
    String slug = "test-post";
    testPost.setSlug(slug);
    when(postCache.get(slug)).thenReturn(Optional.empty());
    when(postRepository.findBySlug(slug)).thenReturn(Optional.of(testPost));
    // when(tagRepository.findByPostId(1L)).thenReturn(new ArrayList<>());

    try (MockedStatic<DtoMapper> dtoMapperMock = mockStatic(DtoMapper.class)) {
      dtoMapperMock.when(() -> DtoMapper.toPostResponse(any(Post.class)))
          .thenReturn(testPostResponse);

      // Act
      PostResponse result = postService.getPostBySlug(slug);

      // Assert
      assertNotNull(result);
      assertEquals(testPostResponse.id(), result.id());
      assertEquals(testPostResponse.slug(), result.slug());
      verify(postCache).put(slug, testPostResponse);
    }
  }

  @Test
  @DisplayName("Should successfully get post by ID from database without cache")
  void getPostById_WithValidId_ShouldReturnPostResponseFromDatabase() throws SQLException {
    // Arrange
    when(postRepository.findById(eq(1L))).thenReturn(Optional.of(testPost));

    try (MockedStatic<DtoMapper> dtoMapperMock = mockStatic(DtoMapper.class)) {
      dtoMapperMock.when(() -> DtoMapper.toPostResponse(any(Post.class)))
          .thenReturn(testPostResponse);

      // Act
      PostResponse result = postService.getPostById(1L);

      // Assert
      assertNotNull(result);
      assertEquals(testPostResponse.id(), result.id());
      verify(postRepository).findById(eq(1L));
    }
  }

  @Test
  @DisplayName("Should successfully get published posts")
  void getPublishedPosts_ShouldReturnPageOfPosts() {
    // Arrange
    PageRequest pageRequest = PageRequest.builder().page(1).size(10).sortBy("created_at").sortDir("desc").build();
    when(postRepository.findPublishedPosts(eq(10), eq(10), eq("created_at"), eq("desc"))).thenReturn(List.of(testPost));
    when(postRepository.countPublishedPosts()).thenReturn(1L);

    try (MockedStatic<DtoMapper> dtoMapperMock = mockStatic(DtoMapper.class)) {
      dtoMapperMock.when(() -> DtoMapper.toPostResponse(any(Post.class)))
          .thenReturn(testPostResponse);
      dtoMapperMock.when(() -> DtoMapper.toPageResponse(anyList(), anyInt(), anyInt(), anyInt()))
          .thenReturn(new PageResponse<>(List.of(testPostResponse), 1, 10, 1, 1, true, false));

      // Act
      PageResponse<PostResponse> result = postService.getPublishedPosts(pageRequest);

      // Assert
      assertNotNull(result);
      assertEquals(1, result.totalElements());
    }
  }

  @Test
  @DisplayName("Should successfully search posts")
  void searchPosts_WithKeyword_ShouldReturnPageOfPosts() {
    // Arrange
    String keyword = "test";
    PageRequest pageRequest = PageRequest.builder().page(1).size(10).sortBy("created_at").sortDir("desc").build();
    when(postRepository.searchPostsByKeyword(eq(keyword), eq(10), eq(10), eq("created_at"), eq("desc"))).thenReturn(List.of(testPost));
    when(postRepository.countPostsByKeyword(eq(keyword))).thenReturn(1L);

    try (MockedStatic<DtoMapper> dtoMapperMock = mockStatic(DtoMapper.class)) {
      dtoMapperMock.when(() -> DtoMapper.toPostResponse(any(Post.class)))
          .thenReturn(testPostResponse);
      dtoMapperMock.when(() -> DtoMapper.toPageResponse(anyList(), anyInt(), anyInt(), anyInt()))
          .thenReturn(new PageResponse<>(List.of(testPostResponse), 1, 10, 1, 1, true, false));

      // Act
      var result = postService.searchPosts(keyword, pageRequest);

      // Assert
      assertNotNull(result);
      assertEquals(1, result.totalElements());
    }
  }

  @Test
  @DisplayName("Should successfully get user posts")
  void getUserPosts_WithUserId_ShouldReturnPageOfPostResponses() {
    // Arrange
    PageRequest pageRequest = PageRequest.builder().page(1).size(10).sortBy("created_at").sortDir("desc").build();
    when(postRepository.findPostsByUser(eq(1L), eq(10), eq(10), eq("created_at"), eq("desc"))).thenReturn(List.of(testPost));
    when(postRepository.countPostsByUser(eq(1L))).thenReturn(1L);

    try (MockedStatic<DtoMapper> dtoMapperMock = mockStatic(DtoMapper.class)) {
      dtoMapperMock.when(() -> DtoMapper.toPostResponse(any(Post.class)))
          .thenReturn(testPostResponse);
      dtoMapperMock.when(() -> DtoMapper.toPageResponse(anyList(), anyInt(), anyInt(), anyInt()))
          .thenReturn(new PageResponse<>(List.of(testPostResponse), 1, 10, 1, 1, true, false));

      // Act
      PageResponse<PostResponse> result = postService.getUserPosts(1L, pageRequest);

      // Assert
      assertNotNull(result);
      assertEquals(1, result.totalElements());
      assertEquals(1, result.content().size());
    }
  }

  @Test
  @DisplayName("Should successfully get posts by category")
  void getPostsByCategory_WithCategoryId_ShouldReturnPageOfPostResponses() {
    // Arrange
    PageRequest pageRequest = PageRequest.builder().page(1).size(10).sortBy("created_at").sortDir("desc").build();
    when(postRepository.findPostsByCategory(eq(1L), eq(10), eq(10), eq("created_at"), eq("desc"))).thenReturn(List.of(testPost));
    when(postRepository.countPostsByCategory(eq(1L))).thenReturn(1L);

    try (MockedStatic<DtoMapper> dtoMapperMock = mockStatic(DtoMapper.class)) {
      dtoMapperMock.when(() -> DtoMapper.toPostResponse(any(Post.class)))
          .thenReturn(testPostResponse);
      dtoMapperMock.when(() -> DtoMapper.toPageResponse(anyList(), anyInt(), anyInt(), anyInt()))
          .thenReturn(new PageResponse<>(List.of(testPostResponse), 1, 10, 1, 1, true, false));

      // Act
      PageResponse<PostResponse> result = postService.getPostsByCategory(1L, pageRequest);

      // Assert
      assertNotNull(result);
      assertEquals(1, result.totalElements());
      assertEquals(1, result.content().size());
    }
  }

  // @Test
  // @DisplayName("Should only update non-null fields in update request")
  // void updatePost_WithPartialData_ShouldOnlyUpdateNonNullFields() {
  // // Arrange
  // UpdatePostRequest partialUpdate = new UpdatePostRequest(
  // "Updated Title Only",
  // null,
  // null,
  // null,
  // null,
  // null,
  // null);

  // String originalContent = testPost.getContent();
  // when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
  // when(postRepository.save(any(Post.class))).thenReturn(testPost);

  // PostResponse partialUpdateResponse = new PostResponse(
  // 1L,
  // testPostResponse.author(),
  // testPostResponse.category(),
  // "updated-title-only", // slug
  // "Updated Title Only",
  // originalContent,
  // testPostResponse.excerpt(),
  // testPostResponse.status(),
  // testPostResponse.createdAt(),
  // LocalDateTime.now(),
  // 0,
  // 0,
  // testPostResponse.coverImage(),
  // testPostResponse.tags());

  // try (MockedStatic<DtoMapper> dtoMapperMock = mockStatic(DtoMapper.class)) {
  // dtoMapperMock
  // .when(() -> DtoMapper.toPostResponse(any(Post.class)))
  // .thenReturn(partialUpdateResponse);

  // // Act
  // PostResponse result = postService.updatePost(1L, partialUpdate, 1L);

  // // Assert
  // assertNotNull(result);
  // assertEquals("Updated Title Only", result.title());
  // assertEquals(originalContent, result.content());
  // }
  // }
}
