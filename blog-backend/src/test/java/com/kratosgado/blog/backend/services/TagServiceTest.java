package com.kratosgado.blog.backend.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.jpa.TagRepository;
import com.kratosgado.blog.backend.cache.CacheConfig.TagCache;
import com.kratosgado.blog.dtos.request.CreateTagRequest;
import com.kratosgado.blog.dtos.request.UpdateTagRequest;
import com.kratosgado.blog.models.Tag;

import java.util.List;

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
  @DisplayName("Should successfully create a tag")
  void createTag_WithValidData_ShouldReturnTag() {
    // Arrange
    CreateTagRequest request = new CreateTagRequest("Java", "Java programming");
    when(tagRepository.findBySlug("java")).thenReturn(Optional.empty());
    when(tagRepository.save(any(Tag.class))).thenReturn(testTag);

    // Act
    Tag result = tagService.createTag(request);

    // Assert
    assertNotNull(result);
  }

  @Test
  @DisplayName("Should throw exception when creating duplicate tag")
  void createTag_WithExistingSlug_ShouldThrowException() {
    // Arrange
    CreateTagRequest request = new CreateTagRequest("Java", "Java programming");
    when(tagRepository.findBySlug("java")).thenReturn(Optional.of(testTag));

    // Act
    BlogException exception = assertThrows(BlogException.class, 
        () -> tagService.createTag(request));

    // Assert
    assertTrue(exception.getMessage().contains("already exists"));
  }

  @ParameterizedTest
  @MethodSource("slugGenerationTestCases")
  @DisplayName("Should generate correct slug from tag name")
  void createTag_ShouldGenerateCorrectSlug(String name, String expectedSlug) {
    // Arrange
    CreateTagRequest request = new CreateTagRequest(name, "Description");
    when(tagRepository.findBySlug(anyString())).thenReturn(Optional.empty());
    when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> {
      Tag tag = invocation.getArgument(0);
      assertEquals(expectedSlug, tag.getSlug());
      return tag;
    });

    // Act
    tagService.createTag(request);

    // Assert - verification done in mock answer
  }

  static Stream<Arguments> slugGenerationTestCases() {
    return Stream.of(
        Arguments.of("Spring Boot", "spring-boot"),
        Arguments.of("C++ & C#", "c-c")
    );
  }

  @Test
  @DisplayName("Should successfully update a tag")
  void updateTag_WithValidData_ShouldReturnUpdatedTag() {
    // Arrange
    UpdateTagRequest request = new UpdateTagRequest("Java SE", "Java Standard Edition");
    when(tagRepository.findById(1L)).thenReturn(Optional.of(testTag));
    when(tagRepository.findBySlug("java-se")).thenReturn(Optional.empty());
    when(tagRepository.save(any(Tag.class))).thenReturn(testTag);

    // Act
    Tag result = tagService.updateTag(1L, request);

    // Assert
    assertNotNull(result);
    assertEquals("Java SE", testTag.getName());
    assertEquals("java-se", testTag.getSlug());
  }

  @Test
  @DisplayName("Should only update description when name is null")
  void updateTag_WithOnlyDescription_ShouldOnlyUpdateDescription() {
    // Arrange
    UpdateTagRequest request = new UpdateTagRequest(null, "Updated description");
    String originalName = testTag.getName();
    when(tagRepository.findById(1L)).thenReturn(Optional.of(testTag));
    when(tagRepository.save(any(Tag.class))).thenReturn(testTag);

    // Act
    Tag result = tagService.updateTag(1L, request);

    // Assert
    assertNotNull(result);
    assertEquals(originalName, testTag.getName());
    assertEquals("Updated description", testTag.getDescription());
  }

  @ParameterizedTest
  @MethodSource("tagNotFoundTestCases")
  @DisplayName("Should throw exception when tag not found")
  void tagNotFound_ShouldThrowException(String operation) {
    // Arrange
    switch (operation) {
      case "update":
        when(tagRepository.findById(1L)).thenReturn(Optional.empty());
        break;
      case "getById":
        when(tagRepository.findById(1L)).thenReturn(Optional.empty());
        when(tagCache.get(1L)).thenReturn(Optional.empty());
        break;
      case "delete":
        when(tagRepository.existsById(1L)).thenReturn(false);
        break;
      case "getBySlug":
        when(tagRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());
        break;
    }

    // Act & Assert
    BlogException exception;
    switch (operation) {
      case "update":
        exception = assertThrows(BlogException.class, 
            () -> tagService.updateTag(1L, new UpdateTagRequest("Java", "Description")));
        break;
      case "delete":
        exception = assertThrows(BlogException.class, 
            () -> tagService.deleteTag(1L));
        break;
      case "getById":
        exception = assertThrows(BlogException.class, 
            () -> tagService.getTagById(1L));
        break;
      case "getBySlug":
        exception = assertThrows(BlogException.class, 
            () -> tagService.getTagBySlug("nonexistent"));
        break;
      default:
        throw new IllegalArgumentException("Unknown operation: " + operation);
    }
    assertTrue(exception.getMessage().contains("not found"));
  }

  static Stream<Arguments> tagNotFoundTestCases() {
    return Stream.of(
        Arguments.of("update"),
        Arguments.of("delete"),
        Arguments.of("getById"),
        Arguments.of("getBySlug")
    );
  }

  @Test
  @DisplayName("Should allow updating with same name")
  void updateTag_WithSameName_ShouldSucceed() {
    // Arrange
    UpdateTagRequest request = new UpdateTagRequest("Java", "Updated description");
    when(tagRepository.findById(1L)).thenReturn(Optional.of(testTag));
    when(tagRepository.save(any(Tag.class))).thenReturn(testTag);

    // Act
    Tag result = tagService.updateTag(1L, request);

    // Assert
    assertNotNull(result);
  }

  @Test
  @DisplayName("Should throw exception when updating to existing tag name")
  void updateTag_WithExistingSlug_ShouldThrowException() {
    // Arrange
    UpdateTagRequest request = new UpdateTagRequest("Python", "Python language");
    when(tagRepository.findById(1L)).thenReturn(Optional.of(testTag));
    when(tagRepository.findBySlug("python")).thenReturn(Optional.of(new Tag()));

    // Act
    BlogException exception = assertThrows(BlogException.class, 
        () -> tagService.updateTag(1L, request));

    // Assert
    assertTrue(exception.getMessage().contains("already exists"));
  }

  @Test
  @DisplayName("Should successfully delete a tag")
  void deleteTag_WithValidId_ShouldDeleteTag() {
    // Arrange
    when(tagRepository.existsById(1L)).thenReturn(true);
    doNothing().when(tagRepository).deleteById(1L);

    // Act
    tagService.deleteTag(1L);

    // Assert - method completes without exception
  }

  @Test
  @DisplayName("Should successfully get tag by ID")
  void getTagById_WithValidId_ShouldReturnTag() {
    // Arrange
    when(tagCache.get(1L)).thenReturn(Optional.empty()); // Cache miss
    when(tagRepository.findById(1L)).thenReturn(Optional.of(testTag));

    // Act
    Tag result = tagService.getTagById(1L);

    // Assert
    assertNotNull(result);
    assertEquals(testTag.getId(), result.getId());
    verify(tagCache).put(1L, testTag); // Verify caching
  }

  @Test
  @DisplayName("Should successfully get tag by slug")
  void getTagBySlug_WithValidSlug_ShouldReturnTag() {
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
    List<Tag> tags = List.of(testTag);
    Page<Tag> tagPage = new PageImpl<>(tags, PageRequest.of(0, 10), 1);
    when(tagRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(tagPage);

    // Act
    var result = tagService.getAllTags(PageRequest.of(0, 10));

    // Assert
    assertNotNull(result);
    assertEquals(1, result.totalElements());
  }

  @Test
  @DisplayName("Should search tags by keyword")
  void searchTags_WithKeyword_ShouldReturnPageOfTags() {
    // Arrange
    String keyword = "java";
    List<Tag> tags = List.of(testTag);
    Page<Tag> tagPage = new PageImpl<>(tags, PageRequest.of(0, 10), 1);
    when(tagRepository.searchByName(eq(keyword), any(org.springframework.data.domain.Pageable.class))).thenReturn(tagPage);

    // Act
    var result = tagService.searchTags(keyword, PageRequest.of(0, 10));

    // Assert
    assertNotNull(result);
    assertEquals(1, result.totalElements());
  }
}
