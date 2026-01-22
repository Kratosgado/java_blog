package com.kratosgado.blog.backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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

import com.kratosgado.blog.backend.cache.CacheConfig.PostCache;
import com.kratosgado.blog.backend.dao.CategoryDAO;
import com.kratosgado.blog.backend.dao.PostDAO;
import com.kratosgado.blog.backend.dao.TagDAO;
import com.kratosgado.blog.backend.dao.UserDAO;
import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.utils.DtoMapper;
import com.kratosgado.blog.dtos.request.CreatePostRequest;
import com.kratosgado.blog.dtos.request.UpdatePostRequest;
import com.kratosgado.blog.dtos.response.AuthorSummary;
import com.kratosgado.blog.dtos.response.CategorySummary;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.PostResponse;
import com.kratosgado.blog.enums.PostStatus;
import com.kratosgado.blog.models.Category;
import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.models.User;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostService Tests")
class PostServiceTest {

  @Mock
  private PostDAO postDAO;

  @Mock
  private TagDAO tagDAO;

  @Mock
  private UserDAO userDAO;

  @Mock
  private CategoryDAO categoryDAO;

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
    testCategory.setId(1);
    testCategory.setName("Test Category");
    testCategory.setSlug("test-category");

    testPost = new Post();
    testPost.setId(1);
    testPost.setUserId(1L);
    testPost.setTitle("Test Post");
    testPost.setContent("Test Content");
    testPost.setExcerpt("Test Excerpt");
    testPost.setStatus("published");
    testPost.setCategoryId(1);
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
        "draft");

    updateRequest = new UpdatePostRequest(
        "Updated Title",
        "Updated Content",
        "Updated Excerpt",
        2L,
        "new-cover.jpg",
        PostStatus.published);
  }

  @Test
  @DisplayName("Should successfully create a post")
  void createPost_WithValidData_ShouldReturnPostResponse() {
    // Arrange
    when(postDAO.createPost(any(Post.class))).thenReturn(Optional.of(testPost));
    when(tagDAO.getTagsByPostId(anyInt())).thenReturn(new ArrayList<>());
    when(categoryDAO.getCategoryById(anyInt())).thenReturn(Optional.of(testCategory));

    try (MockedStatic<DtoMapper> dtoMapperMock = mockStatic(DtoMapper.class)) {
      dtoMapperMock
          .when(() -> DtoMapper.toPostResponse(any(Post.class), any(User.class), any(Category.class), anyList()))
          .thenReturn(testPostResponse);

      // Act
      PostResponse result = postService.createPost(createRequest, testUser);

      // Assert
      assertNotNull(result);
      assertEquals(testPostResponse.id(), result.id());
      assertEquals(testPostResponse.title(), result.title());
    }
  }

  @Test
  @DisplayName("Should successfully update a post")
  void updatePost_WithValidData_ShouldReturnUpdatedPostResponse() {
    // Arrange
    when(postDAO.getPostById(1)).thenReturn(Optional.of(testPost));
    when(postDAO.updatePost(any(Post.class))).thenReturn(Optional.of(testPost));
    when(tagDAO.getTagsByPostId(anyInt())).thenReturn(new ArrayList<>());
    when(userDAO.getUserById(anyLong())).thenReturn(Optional.of(testUser));
    when(categoryDAO.getCategoryById(anyInt())).thenReturn(Optional.of(testCategory));

    PostResponse updatedResponse = new PostResponse(
        1L,
        testPostResponse.author(),
        testPostResponse.category(),
        testPostResponse.slug(),
        updateRequest.title(),
        updateRequest.content(),
        updateRequest.excerpt(),
        updateRequest.status(),
        testPostResponse.createdAt(),
        LocalDateTime.now(),
        0,
        0,
        updateRequest.coverImage(),
        testPostResponse.tags());

    try (MockedStatic<DtoMapper> dtoMapperMock = mockStatic(DtoMapper.class)) {
      dtoMapperMock
          .when(() -> DtoMapper.toPostResponse(any(Post.class), any(User.class), any(Category.class), anyList()))
          .thenReturn(updatedResponse);

      // Act
      PostResponse result = postService.updatePost(1L, updateRequest, 1L);

      // Assert
      assertNotNull(result);
      assertEquals(updateRequest.title(), result.title());
      assertEquals(updateRequest.content(), result.content());
    }
  }

  @ParameterizedTest
  @MethodSource("postNotFoundTestCases")
  @DisplayName("Should throw exception when post not found")
  void postNotFound_ShouldThrowException(String operation) {
    // Arrange
    switch (operation) {
      case "update":
      case "delete":
        when(postDAO.getPostById(1)).thenReturn(Optional.empty());
        break;
      case "getById":
        when(postDAO.getPostById(1)).thenReturn(Optional.empty());
        break;
    }

    // Act & Assert
    BlogException exception;
    switch (operation) {
      case "update":
        exception = assertThrows(BlogException.class,
            () -> postService.updatePost(1L, updateRequest, 1L));
        assertTrue(exception.getMessage().contains("not found") ||
            exception.getMessage().contains("permission"));
        break;
      case "delete":
        exception = assertThrows(BlogException.class,
            () -> postService.deletePost(1L, 1L));
        assertTrue(exception.getMessage().contains("not found") ||
            exception.getMessage().contains("permission"));
        break;
      case "getById":
        exception = assertThrows(BlogException.class,
            () -> postService.getPostById(1L));
        assertEquals("Post not found", exception.getMessage());
        break;
      default:
        throw new IllegalArgumentException("Unknown operation: " + operation);
    }
  }

  static Stream<Arguments> postNotFoundTestCases() {
    return Stream.of(
        Arguments.of("update"),
        Arguments.of("delete"),
        Arguments.of("getById"));
  }

  @Test
  @DisplayName("Should successfully delete a post")
  void deletePost_WithValidId_ShouldDeletePost() {
    // Arrange
    when(postDAO.getPostById(1)).thenReturn(Optional.of(testPost));
    when(postDAO.deletePost(1)).thenReturn(true);

    // Act
    postService.deletePost(1L, 1L);

    // Assert
    verify(postDAO).deletePost(1);
  }

  @Test
  @DisplayName("Should successfully get post by slug from cache")
  void getPostBySlug_WithValidSlug_ShouldReturnPostResponseFromCache() {
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
    verify(postDAO, never()).getPostBySlug(slug);
  }

  @Test
  @DisplayName("Should successfully get post by slug from database on cache miss")
  void getPostBySlug_OnCacheMiss_ShouldFetchFromDatabase() {
    // Arrange
    String slug = "test-post";
    testPost.setSlug(slug);
    when(postCache.get(slug)).thenReturn(Optional.empty());
    when(postDAO.getPostBySlug(slug)).thenReturn(Optional.of(testPost));
    when(tagDAO.getTagsByPostId(1)).thenReturn(new ArrayList<>());
    doNothing().when(postDAO).incrementViews(anyInt());

    try (MockedStatic<DtoMapper> dtoMapperMock = mockStatic(DtoMapper.class)) {
      dtoMapperMock.when(() -> DtoMapper.toPostResponse(any(Post.class), anyList()))
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
  void getPostById_WithValidId_ShouldReturnPostResponseFromDatabase() {
    // Arrange
    when(postDAO.getPostById(1)).thenReturn(Optional.of(testPost));
    when(tagDAO.getTagsByPostId(1)).thenReturn(new ArrayList<>());
    doNothing().when(postDAO).incrementViews(anyInt());

    try (MockedStatic<DtoMapper> dtoMapperMock = mockStatic(DtoMapper.class)) {
      dtoMapperMock.when(() -> DtoMapper.toPostResponse(any(Post.class), anyList()))
          .thenReturn(testPostResponse);

      // Act
      PostResponse result = postService.getPostById(1L);

      // Assert
      assertNotNull(result);
      assertEquals(testPostResponse.id(), result.id());
      verify(postDAO).getPostById(1);
    }
  }

  @Test
  @DisplayName("Should successfully get published posts")
  void getPublishedPosts_ShouldReturnPageOfPosts() {
    // Arrange
    when(postDAO.getPostsPaginated(anyInt(), anyInt())).thenReturn(List.of(testPost));
    when(postDAO.getPublishedPostCount()).thenReturn(1);
    when(tagDAO.getTagsByPostId(anyInt())).thenReturn(new ArrayList<>());

    try (MockedStatic<DtoMapper> dtoMapperMock = mockStatic(DtoMapper.class)) {
      dtoMapperMock.when(() -> DtoMapper.toPostResponse(any(Post.class), anyList()))
          .thenReturn(testPostResponse);
      dtoMapperMock.when(() -> DtoMapper.toPageResponse(anyList(), anyInt(), anyInt(), anyInt()))
          .thenReturn(new PageResponse<>(List.of(testPostResponse), 1, 1, 1, 1, true, false));

      // Act
      PageResponse<PostResponse> result = postService.getPublishedPosts(1, 10);

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
    when(postDAO.searchPostsByKeyword(keyword)).thenReturn(List.of(testPost));
    when(tagDAO.getTagsByPostId(anyInt())).thenReturn(new ArrayList<>());

    try (MockedStatic<DtoMapper> dtoMapperMock = mockStatic(DtoMapper.class)) {
      dtoMapperMock.when(() -> DtoMapper.toPostResponse(any(Post.class), anyList()))
          .thenReturn(testPostResponse);
      dtoMapperMock.when(() -> DtoMapper.toPageResponse(anyList(), anyInt(), anyInt(), anyInt()))
          .thenReturn(new PageResponse<>(List.of(testPostResponse), 1, 1, 1, 1, true, false));

      // Act
      var result = postService.searchPosts(keyword, 1, 10);

      // Assert
      assertNotNull(result);
      assertEquals(1, result.totalElements());
    }
  }

  @Test
  @DisplayName("Should successfully get user posts")
  void getUserPosts_WithUserId_ShouldReturnPageOfPostResponses() {
    // Arrange
    when(postDAO.getPostsByUserIdPaginated(1, 1, 10)).thenReturn(List.of(testPost));
    when(postDAO.getPostsByUserId(1)).thenReturn(List.of(testPost));
    when(tagDAO.getTagsByPostId(anyInt())).thenReturn(new ArrayList<>());

    try (MockedStatic<DtoMapper> dtoMapperMock = mockStatic(DtoMapper.class)) {
      dtoMapperMock.when(() -> DtoMapper.toPostResponse(any(Post.class), anyList()))
          .thenReturn(testPostResponse);
      dtoMapperMock.when(() -> DtoMapper.toPageResponse(anyList(), anyInt(), anyInt(), anyInt()))
          .thenReturn(new PageResponse<>(List.of(testPostResponse), 1, 1, 1, 1, true, false));

      // Act
      PageResponse<PostResponse> result = postService.getUserPosts(1L, 1, 10);

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
    when(postDAO.getPostsByCategoryId(1)).thenReturn(List.of(testPost));
    when(tagDAO.getTagsByPostId(anyInt())).thenReturn(new ArrayList<>());

    try (MockedStatic<DtoMapper> dtoMapperMock = mockStatic(DtoMapper.class)) {
      dtoMapperMock.when(() -> DtoMapper.toPostResponse(any(Post.class), anyList()))
          .thenReturn(testPostResponse);
      dtoMapperMock.when(() -> DtoMapper.toPageResponse(anyList(), anyInt(), anyInt(), anyInt()))
          .thenReturn(new PageResponse<>(List.of(testPostResponse), 1, 1, 1, 1, true, false));

      // Act
      PageResponse<PostResponse> result = postService.getPostsByCategory(1L, 1, 10);

      // Assert
      assertNotNull(result);
      assertEquals(1, result.totalElements());
      assertEquals(1, result.content().size());
    }
  }

  @Test
  @DisplayName("Should only update non-null fields in update request")
  void updatePost_WithPartialData_ShouldOnlyUpdateNonNullFields() {
    // Arrange
    UpdatePostRequest partialUpdate = new UpdatePostRequest(
        "Updated Title Only",
        null,
        null,
        null,
        null,
        null);

    String originalContent = testPost.getContent();
    when(postDAO.getPostById(1)).thenReturn(Optional.of(testPost));
    when(postDAO.updatePost(any(Post.class))).thenReturn(Optional.of(testPost));
    when(tagDAO.getTagsByPostId(anyInt())).thenReturn(new ArrayList<>());
    when(userDAO.getUserById(anyLong())).thenReturn(Optional.of(testUser));
    when(categoryDAO.getCategoryById(anyInt())).thenReturn(Optional.of(testCategory));

    PostResponse partialUpdateResponse = new PostResponse(
        1L,
        testPostResponse.author(),
        testPostResponse.category(),
        "updated-title-only", // slug
        "Updated Title Only",
        originalContent,
        testPostResponse.excerpt(),
        testPostResponse.status(),
        testPostResponse.createdAt(),
        LocalDateTime.now(),
        0,
        0,
        testPostResponse.coverImage(),
        testPostResponse.tags());

    try (MockedStatic<DtoMapper> dtoMapperMock = mockStatic(DtoMapper.class)) {
      dtoMapperMock
          .when(() -> DtoMapper.toPostResponse(any(Post.class), any(User.class), any(Category.class), anyList()))
          .thenReturn(partialUpdateResponse);

      // Act
      PostResponse result = postService.updatePost(1L, partialUpdate, 1L);

      // Assert
      assertNotNull(result);
      assertEquals("Updated Title Only", result.title());
      assertEquals(originalContent, result.content());
    }
  }
}
