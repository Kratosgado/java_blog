package com.kratosgado.blog.backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.jpa.CategoryRepository;
import com.kratosgado.blog.backend.repositories.jpa.PostRepository;
import com.kratosgado.blog.backend.repositories.jpa.TagRepository;
import com.kratosgado.blog.backend.repositories.jpa.UserRepository;
import com.kratosgado.blog.backend.utils.DtoMapper;
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
  private PostRepository postRepository;

  @Mock
  private TagRepository tagRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private CategoryRepository categoryRepository;

  @InjectMocks
  private PostService postService;

  private Post testPost;
  private User testUser;
  private Category testCategory;
  private PostResponse testPostResponse;
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

    updateRequest = new UpdatePostRequest(
        "Updated Title",
        "Updated Content",
        "Updated Excerpt",
        1L,
        "new-cover.jpg",
        PostStatus.published,
        new Long[] { 3L });
  }

  @Test
  @DisplayName("Should throw exception when post not found for update")
  void postNotFound_ShouldThrowException_forUpdate() {
    // Arrange
    when(postRepository.findById(eq(1L))).thenReturn(Optional.empty());

    // Act & Assert
    BlogException exception = assertThrows(BlogException.class,
        () -> postService.updatePost(1L, updateRequest, 1L));
    assertEquals("Post not found", exception.getMessage());
  }

  @Test
  @DisplayName("Should successfully get post by slug")
  void getPostBySlug_WithValidSlug_ShouldReturnPostResponse() {
    // Arrange
    String slug = "test-post";
    when(postRepository.findBySlug(slug)).thenReturn(Optional.of(testPost));

    // Act
    PostResponse result = postService.getPostBySlug(slug);

    // Assert
    assertNotNull(result);
    assertEquals(testPost.getId(), result.id());
    assertEquals(testPost.getSlug(), result.slug());
    verify(postRepository).findBySlug(slug);
  }

  @Test
  @DisplayName("Should successfully get post by ID from database")
  void getPostById_WithValidId_ShouldReturnPostResponseFromDatabase() {
    // Arrange
    when(postRepository.findById(eq(1L))).thenReturn(Optional.of(testPost));

    // Act
    PostResponse result = postService.getPostById(1L);

    // Assert
    assertNotNull(result);
    assertEquals(testPost.getId(), result.id());
    verify(postRepository).findById(eq(1L));
  }

  // @Test
  // @DisplayName("Should successfully get published posts")
  // void getPublishedPosts_ShouldReturnPageOfPosts() {
  // // Arrange
  // com.kratosgado.blog.dtos.request.PageRequest pageRequest =
  // com.kratosgado.blog.dtos.request.PageRequest.builder()
  // .page(0).size(10).sortBy("createdAt").sortDir("DESC").build();
  // Page<Post> page = new PageImpl<>(List.of(testPost));
  // when(postRepository.findByStatus(eq(PostStatus.published),
  // any(Pageable.class))).thenReturn(page);
  //
  // // Act
  // PageResponse<PostResponse> result =
  // postService.getPublishedPosts(pageRequest);
  //
  // // Assert
  // assertNotNull(result);
  // assertEquals(1, result.totalElements());
  // }

  @Test
  @DisplayName("Should successfully search posts")
  void searchPosts_WithKeyword_ShouldReturnPageOfPosts() {
    // Arrange
    String keyword = "test";
    com.kratosgado.blog.dtos.request.PageRequest pageRequest = com.kratosgado.blog.dtos.request.PageRequest.builder()
        .page(0).size(10).sortBy("createdAt").sortDir("DESC").build();
    Page<Post> page = new PageImpl<>(List.of(testPost));
    when(postRepository.searchPublishedPosts(eq(keyword), any(Pageable.class))).thenReturn(page);

    // Act
    var result = postService.searchPosts(keyword, pageRequest);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.totalElements());
  }

  @Test
  @DisplayName("Should successfully get user posts")
  void getUserPosts_WithUserId_ShouldReturnPageOfPostResponses() {
    // Arrange
    com.kratosgado.blog.dtos.request.PageRequest pageRequest = com.kratosgado.blog.dtos.request.PageRequest.builder()
        .page(0).size(10).sortBy("createdAt").sortDir("DESC").build();
    Page<Post> page = new PageImpl<>(List.of(testPost));
    when(postRepository.findByUserId(eq(1L), any(Pageable.class))).thenReturn(page);

    // Act
    PageResponse<PostResponse> result = postService.getUserPosts(1L, pageRequest);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.totalElements());
    assertEquals(1, result.content().size());
  }

  @Test
  @DisplayName("Should successfully get posts by category")
  void getPostsByCategory_WithCategoryId_ShouldReturnPageOfPostResponses() {
    // Arrange
    com.kratosgado.blog.dtos.request.PageRequest pageRequest = com.kratosgado.blog.dtos.request.PageRequest.builder()
        .page(0).size(10).sortBy("createdAt").sortDir("DESC").build();
    Page<Post> page = new PageImpl<>(List.of(testPost));
    when(postRepository.findByCategoryId(eq(1L), any(Pageable.class))).thenReturn(page);

    // Act
    PageResponse<PostResponse> result = postService.getPostsByCategory(1L, pageRequest);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.totalElements());
    assertEquals(1, result.content().size());
  }

}
