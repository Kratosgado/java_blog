package com.kratosgado.blog.backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.jpa.TagRepository;
import com.kratosgado.blog.dtos.request.CreateTagRequest;
import com.kratosgado.blog.dtos.request.PageRequest;
import com.kratosgado.blog.dtos.request.UpdateTagRequest;
import com.kratosgado.blog.models.Tag;

@ExtendWith(MockitoExtension.class)
@DisplayName("TagService Tests")
class TagServiceTest {

  @Mock
  private TagRepository tagRepository;

  @InjectMocks
  private TagService tagService;

  private Tag testTag;

  @BeforeEach
  void setUp() {
    testTag = new Tag(1L, "Java", "java", "Java programming language");
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
  @MethodSource("tagNotFoundTestCases")
  @DisplayName("Should throw exception when tag not found")
  void tagNotFound_ShouldThrowException(String operation) {
    // Arrange
    switch (operation) {
      case "update":
      case "getById":
        when(tagRepository.findById(eq(1L))).thenReturn(Optional.empty());
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
        case "getById":
          tagService.getTagById(1L);
          break;
        case "getBySlug":
          tagService.getTagBySlug("nonexistent");
          break;
        case "delete":
          doThrow(new RuntimeException("Tag not found")).when(tagRepository).deleteById(eq(1L));
          tagService.deleteTag(1L);
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
  void getTagById_WithValidId_ShouldReturnTag() {
    // Arrange
    when(tagRepository.findById(eq(1L))).thenReturn(Optional.of(testTag));

    // Act
    Tag result = tagService.getTagById(1L);

    // Assert
    assertNotNull(result);
    assertEquals(testTag.getId(), result.getId());
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
    PageRequest pageRequest = PageRequest.builder().page(0).size(10).sortBy("name").sortDir("ASC").build();
    Page<Tag> page = new PageImpl<>(List.of(testTag));
    when(tagRepository.findAll(any(Pageable.class))).thenReturn(page);

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
    PageRequest pageRequest = PageRequest.builder().page(0).size(10).sortBy("name").sortDir("ASC").build();
    Page<Tag> page = new PageImpl<>(List.of(testTag));
    when(tagRepository.searchByKeyword(eq(keyword), any(Pageable.class))).thenReturn(page);

    // Act
    var result = tagService.searchTags(keyword, pageRequest);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.totalElements());
  }
}
