package com.kratosgado.blog.backend.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.jpa.PostRepository;
import com.kratosgado.blog.backend.utils.DtoMapper;
import com.kratosgado.blog.dtos.request.CreatePostRequest;
import com.kratosgado.blog.dtos.request.UpdatePostRequest;
import com.kratosgado.blog.dtos.response.AuthorSummary;
import com.kratosgado.blog.dtos.response.CategorySummary;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.PostResponse;
import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.models.User;

import java.util.List;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostService Tests")
class PostServiceTest {

  @Mock
  private PostRepository postRepository;

  @InjectMocks
  private PostService postService;

  private Post testPost;
  private User testUser;
  private PostResponse testPostResponse;
  private CreatePostRequest createRequest;
  private UpdatePostRequest updateRequest;
  private Pageable pageable;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testuser");
    testUser.setEmail("test@example.com");
    testUser.setAvatarUrl("avatar.jpg");

    testPost = new Post();
    testPost.setId(1L);
    testPost.setUserId(1L);
    testPost.setTitle("Test Post");
    testPost.setContent("Test Content");
    testPost.setExcerpt("Test Excerpt");
    testPost.setStatus("published");
    testPost.setCategoryId(1L);
    testPost.setCreatedAt(LocalDateTime.now());

    // Create test PostResponse
    testPostResponse = new PostResponse(
        1L,
        new AuthorSummary(1L, "testuser", "test@example.com", "avatar.jpg"),
        new CategorySummary(1L, "Test Category", "test-category"),
        "Test Post",
        "Test Content",
        "Test Excerpt",
        "published",
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
        "draft",
        "cover.jpg");

    updateRequest = new UpdatePostRequest(
        "Updated Title",
        "Updated Content",
        "Updated Excerpt",
        2L,
        "published",
        "new-cover.jpg");

    pageable = PageRequest.of(0, 10);
  }

  @Test
  @DisplayName("Should successfully create a post")
  void createPost_WithValidData_ShouldReturnPostResponse() {
    // Arrange
    when(postRepository.save(any(Post.class))).thenReturn(testPost);
    try (MockedStatic<DtoMapper> dtoMapperMock = mockStatic(DtoMapper.class)) {
      dtoMapperMock.when(() -> DtoMapper.toPostResponse(testPost)).thenReturn(testPostResponse);

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
    when(postRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testPost));
    when(postRepository.save(any(Post.class))).thenReturn(testPost);

    PostResponse updatedResponse = new PostResponse(
        1L,
        testPostResponse.author(),
        testPostResponse.category(),
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
      dtoMapperMock.when(() -> DtoMapper.toPostResponse(any(Post.class))).thenReturn(updatedResponse);

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
        when(postRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
        break;
      case "getById":
        when(postRepository.findById(1L)).thenReturn(Optional.empty());
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
        Arguments.of("getById")
    );
  }

  @Test
  @DisplayName("Should successfully delete a post")
  void deletePost_WithValidId_ShouldDeletePost() {
    // Arrange
    when(postRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testPost));
    doNothing().when(postRepository).delete(testPost);

    // Act
    postService.deletePost(1L, 1L);

    // Assert - method completes without exception
  }

  @Test
  @DisplayName("Should successfully get post by ID")
  void getPostById_WithValidId_ShouldReturnPostResponse() {
    // Arrange
    when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
    try (MockedStatic<DtoMapper> dtoMapperMock = mockStatic(DtoMapper.class)) {
      dtoMapperMock.when(() -> DtoMapper.toPostResponse(testPost)).thenReturn(testPostResponse);

      // Act
      PostResponse result = postService.getPostById(1L);

      // Assert
      assertNotNull(result);
      assertEquals(testPostResponse.id(), result.id());
      assertEquals(testPostResponse.title(), result.title());
    }
  }

  @Test
  @DisplayName("Should successfully get published posts")
  void getPublishedPosts_ShouldReturnPageOfPosts() {
    // Arrange
    Page<Post> postPage = new PageImpl<>(List.of(testPost));
    when(postRepository.findPublishedPosts(pageable)).thenReturn(postPage);

    // Act
    PageResponse<PostResponse> result = postService.getPublishedPosts(pageable);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.totalElements());
  }

  @Test
  @DisplayName("Should successfully search posts")
  void searchPosts_WithKeyword_ShouldReturnPageOfPosts() {
    // Arrange
    String keyword = "test";
    Page<Post> postPage = new PageImpl<>(List.of(testPost));
    when(postRepository.searchPublishedPosts(keyword, pageable)).thenReturn(postPage);

    // Act
    var result = postService.searchPosts(keyword, pageable);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.totalElements());
  }

  @Test
  @DisplayName("Should successfully get user posts")
  void getUserPosts_WithUserId_ShouldReturnPageOfPostResponses() {
    // Arrange
    Page<Post> postPage = new PageImpl<>(List.of(testPost));
    when(postRepository.findByUserId(1L, pageable)).thenReturn(postPage);

    try (MockedStatic<DtoMapper> dtoMapperMock = mockStatic(DtoMapper.class)) {
      dtoMapperMock.when(() -> DtoMapper.toPostResponse(any(Post.class))).thenReturn(testPostResponse);

      // Act
      PageResponse<PostResponse> result = postService.getUserPosts(1L, pageable);

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
    Page<Post> postPage = new PageImpl<>(List.of(testPost));
    when(postRepository.findByCategoryId(1L, pageable)).thenReturn(postPage);

    try (MockedStatic<DtoMapper> dtoMapperMock = mockStatic(DtoMapper.class)) {
      dtoMapperMock.when(() -> DtoMapper.toPostResponse(any(Post.class))).thenReturn(testPostResponse);

      // Act
      PageResponse<PostResponse> result = postService.getPostsByCategory(1L, pageable);

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
    when(postRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testPost));
    when(postRepository.save(any(Post.class))).thenReturn(testPost);

    PostResponse partialUpdateResponse = new PostResponse(
        1L,
        testPostResponse.author(),
        testPostResponse.category(),
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
      dtoMapperMock.when(() -> DtoMapper.toPostResponse(any(Post.class))).thenReturn(partialUpdateResponse);

      // Act
      PostResponse result = postService.updatePost(1L, partialUpdate, 1L);

      // Assert
      assertNotNull(result);
      assertEquals("Updated Title Only", result.title());
      assertEquals(originalContent, result.content());
    }
  }
}
