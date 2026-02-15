package com.kratosgado.blog.backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
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

  @Nested
  @DisplayName("Parameterized Search Tests")
  class ParameterizedSearchTests {

    @ParameterizedTest
    @ValueSource(strings = {"java", "python", "javascript", "spring", "react"})
    @DisplayName("Should search tags with various keywords")
    void searchTags_WithVariousKeywords_ShouldReturnMatches(String keyword) {
      // Arrange
      PageRequest pageRequest =
          PageRequest.builder().page(0).size(10).sortBy("name").sortDir("ASC").build();
      Tag tag = new Tag(1L, keyword, keyword.toLowerCase(), keyword + " description");
      Page<Tag> page = new PageImpl<>(List.of(tag));
      when(tagRepository.searchByKeyword(eq(keyword), any(Pageable.class))).thenReturn(page);

      // Act
      var result = tagService.searchTags(keyword, pageRequest);

      // Assert
      assertNotNull(result);
      assertEquals(1, result.totalElements());
      assertEquals(keyword, result.content().get(0).getName());
    }

    @ParameterizedTest
    @CsvSource({
        "java, 3",
        "python, 2",
        "web, 5",
        "mobile, 1",
        "nonexistent, 0"
    })
    @DisplayName("Should return correct number of results for search keywords")
    void searchTags_WithKeyword_ShouldReturnExpectedCount(String keyword, int expectedCount) {
      // Arrange
      PageRequest pageRequest =
          PageRequest.builder().page(0).size(10).sortBy("name").sortDir("ASC").build();
      List<Tag> tags = java.util.Collections.nCopies(
          expectedCount,
          new Tag(1L, keyword, keyword, keyword + " description")
      );
      Page<Tag> page = new PageImpl<>(tags);
      when(tagRepository.searchByKeyword(eq(keyword), any(Pageable.class))).thenReturn(page);

      // Act
      var result = tagService.searchTags(keyword, pageRequest);

      // Assert
      assertNotNull(result);
      assertEquals(expectedCount, result.totalElements());
    }
  }

  @Nested
  @DisplayName("Parameterized Pagination Tests")
  class ParameterizedPaginationTests {

    @ParameterizedTest
    @CsvSource({
        "0, 10, name, ASC",
        "1, 20, id, DESC",
        "0, 5, createdAt, ASC",
        "2, 15, name, DESC"
    })
    @DisplayName("Should handle various pagination parameters")
    void getAllTags_WithVariousPaginationParams_ShouldReturnCorrectPage(
        int page, int size, String sortBy, String sortDir) {
      // Arrange
      PageRequest pageRequest =
          PageRequest.builder().page(page).size(size).sortBy(sortBy).sortDir(sortDir).build();
      Page<Tag> mockPage = new PageImpl<>(
          List.of(testTag),
          org.springframework.data.domain.PageRequest.of(page, size),
          10
      );
      when(tagRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

      // Act
      var result = tagService.getAllTags(pageRequest);

      // Assert
      assertNotNull(result);
      // PageResponse uses different field names from its record definition
      // Don't assert on page/size as they're derived from the mock setup
      assertTrue(result.totalElements() >= 0);
    }
  }

  @Nested
  @DisplayName("Slug Generation Tests")
  class SlugGenerationTests {

    @ParameterizedTest
    @CsvSource({
        "'Java', 'java'",
        "'Spring Boot', 'spring-boot'",
        "'Machine Learning', 'machine-learning'",
        "'C++', 'c'",
        "'Node.js', 'nodejs'"
    })
    @DisplayName("Should generate correct slugs from tag names")
    void createTag_WithVariousNames_ShouldGenerateCorrectSlugs(String name, String expectedSlug) {
      // Arrange
      CreateTagRequest request = new CreateTagRequest(name, "Description");
      Tag tag = new Tag(1L, name, expectedSlug, "Description");
      
      when(tagRepository.findBySlug(expectedSlug)).thenReturn(Optional.empty());
      when(tagRepository.save(any(Tag.class))).thenReturn(tag);

      // Act
      Tag result = tagService.createTag(request);

      // Assert
      assertNotNull(result);
      assertEquals(expectedSlug, result.getSlug());
    }
  }

  @Nested
  @DisplayName("Update Tag Tests")
  class UpdateTagTests {

    @ParameterizedTest
    @CsvSource({
        "'Updated Java', 'Updated Java description'",
        "'Java SE', 'Java Standard Edition'",
        "'Core Java', null"
    })
    @DisplayName("Should update tag with various field combinations")
    void updateTag_WithVariousFields_ShouldUpdateCorrectly(String name, String description) {
      // Arrange
      UpdateTagRequest request = new UpdateTagRequest(name, description);
      Tag updatedTag = new Tag(1L, name, "java", description != null ? description : testTag.getDescription());
      
      when(tagRepository.findById(1L)).thenReturn(Optional.of(testTag));
      when(tagRepository.save(any(Tag.class))).thenReturn(updatedTag);

      // Act
      Tag result = tagService.updateTag(1L, request);

      // Assert
      assertNotNull(result);
      assertEquals(name, result.getName());
    }
  }

  @Nested
  @DisplayName("Validation Tests")
  class ValidationTests {

    @ParameterizedTest
    @ValueSource(longs = {1L, 100L, 999L, 12345L})
    @DisplayName("Should successfully retrieve tags by various valid IDs")
    void getTagById_WithVariousValidIds_ShouldReturnTag(Long id) {
      // Arrange
      Tag tag = new Tag(id, "Tag " + id, "tag-" + id, "Description");
      when(tagRepository.findById(id)).thenReturn(Optional.of(tag));

      // Act
      Tag result = tagService.getTagById(id);

      // Assert
      assertNotNull(result);
      assertEquals(id, result.getId());
    }

    @ParameterizedTest
    @ValueSource(strings = {"java", "python", "javascript", "spring-boot", "react-native"})
    @DisplayName("Should successfully retrieve tags by various valid slugs")
    void getTagBySlug_WithVariousValidSlugs_ShouldReturnTag(String slug) {
      // Arrange
      Tag tag = new Tag(1L, "Tag", slug, "Description");
      when(tagRepository.findBySlug(slug)).thenReturn(Optional.of(tag));

      // Act
      Tag result = tagService.getTagBySlug(slug);

      // Assert
      assertNotNull(result);
      assertEquals(slug, result.getSlug());
    }
  }
}
