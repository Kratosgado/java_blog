package com.kratosgado.blog.backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
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
import org.mockito.junit.jupiter.MockitoExtension;

import com.kratosgado.blog.backend.cache.CacheConfig.TagCache;
import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.jdbc.TagRepository;
import com.kratosgado.blog.dtos.request.CreateTagRequest;
import com.kratosgado.blog.dtos.request.PageRequest;
import com.kratosgado.blog.dtos.request.UpdateTagRequest;
import com.kratosgado.blog.models.Tag;

@ExtendWith(MockitoExtension.class)
@DisplayName("TagService Tests")
class TagServiceTest {

  @Mock
  private TagRepository tagRepository;

  @Mock
  private TagCache tagCache;

  @InjectMocks
  private TagService tagService;

  private Tag testTag;

  @BeforeEach
  void setUp() {
    testTag = new Tag("Java", "java", "Java programming language");
    testTag.setId(1L);
  }

  @Test
  @DisplayName("Should throw exception when creating duplicate tag")
  void createTag_WithExistingSlug_ShouldThrowException() throws SQLException {
    // Arrange
    CreateTagRequest request = new CreateTagRequest("Java", "Java programming");
    when(tagRepository.findBySlug("java")).thenReturn(Optional.of(testTag));

    // Act
    BlogException exception = assertThrows(BlogException.class,
        () -> tagService.createTag(request));

    // Assert
    assertTrue(exception.getMessage().contains("already exists"));
  }

  static Stream<Arguments> slugGenerationTestCases() {
    return Stream.of(
        Arguments.of("Spring Boot", "spring-boot"),
        Arguments.of("C++ & C#", "c-c"));
  }

  @ParameterizedTest
  @MethodSource("tagNotFoundTestCases")
  @DisplayName("Should throw exception when tag not found")
  void tagNotFound_ShouldThrowException(String operation) throws SQLException {
    // Arrange
    switch (operation) {
      case "update":
        when(tagRepository.findById(eq(1L))).thenReturn(Optional.empty());
        break;
      case "getById":
        when(tagRepository.findById(eq(1L))).thenReturn(Optional.empty());
        when(tagCache.get(1L)).thenReturn(Optional.empty());
        break;
      case "delete":
        doThrow(new RuntimeException("Tag not found")).when(tagRepository).deleteById(eq(1L));
        break;
      case "getBySlug":
        when(tagRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());
        break;
    }

    // Act & Assert
    assertThrows(RuntimeException.class, () -> {
      switch (operation) {
        case "update":
          tagService.updateTag(1L, new UpdateTagRequest("Java", "Description"));
          break;
        case "delete":
          tagService.deleteTag(1L);
          break;
        case "getById":
          tagService.getTagById(1L);
          break;
        case "getBySlug":
          tagService.getTagBySlug("nonexistent");
          break;
        default:
          throw new IllegalArgumentException("Unknown operation: " + operation);
      }
    });
  }

  static Stream<Arguments> tagNotFoundTestCases() {
    return Stream.of(
        Arguments.of("update"),
        Arguments.of("delete"),
        Arguments.of("getById"),
        Arguments.of("getBySlug"));
  }

  @Test
  @DisplayName("Should successfully get tag by ID")
  void getTagById_WithValidId_ShouldReturnTag() throws SQLException {
    // Arrange
    when(tagCache.get(1L)).thenReturn(Optional.empty()); // Cache miss
    when(tagRepository.findById(eq(1L))).thenReturn(Optional.of(testTag));

    // Act
    Tag result = tagService.getTagById(1L);

    // Assert
    assertNotNull(result);
    assertEquals(testTag.getId(), result.getId());
    verify(tagCache).put(1L, testTag); // Verify caching
  }

  @Test
  @DisplayName("Should successfully get tag by slug")
  void getTagBySlug_WithValidSlug_ShouldReturnTag() throws SQLException {
    // Arrange
    when(tagRepository.findBySlug("java")).thenReturn(Optional.of(testTag));

    // Act
    Tag result = tagService.getTagBySlug("java");

    // Assert
    assertNotNull(result);
    assertEquals(testTag.getSlug(), result.getSlug());
  }

  @Test
  @DisplayName("Should get all tags with pagination")
  void getAllTags_ShouldReturnPageOfTags() {
    // Arrange
    PageRequest pageRequest = PageRequest.builder().page(0).size(10).sortBy("name").sortDir("asc").build();
    when(tagRepository.findAll(eq(10), eq(0), eq("name"), eq("asc"))).thenReturn(List.of(testTag));
    when(tagRepository.count()).thenReturn(1L);

    // Act
    var result = tagService.getAllTags(pageRequest);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.totalElements());
  }

  @Test
  @DisplayName("Should search tags by keyword")
  void searchTags_WithKeyword_ShouldReturnPageOfTags() {
    // Arrange
    String keyword = "java";
    PageRequest pageRequest = PageRequest.builder().page(0).size(10).sortBy("name").sortDir("asc").build();
    when(tagRepository.searchByKeyword(eq(keyword), eq(10), eq(0), eq("name"), eq("asc"))).thenReturn(List.of(testTag));
    when(tagRepository.countByKeyword(eq(keyword))).thenReturn(1L);

    // Act
    var result = tagService.searchTags(keyword, pageRequest);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.totalElements());
  }
}
