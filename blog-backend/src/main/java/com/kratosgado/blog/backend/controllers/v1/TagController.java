package com.kratosgado.blog.backend.controllers.v1;

import com.kratosgado.blog.backend.annotations.OpenApi.DeleteEndpoint;
import com.kratosgado.blog.backend.annotations.OpenApi.GetEndpoint;
import com.kratosgado.blog.backend.annotations.OpenApi.SecuredCreateEndpoint;
import com.kratosgado.blog.backend.annotations.OpenApi.SecuredUpdateEndpoint;
import com.kratosgado.blog.backend.services.TagService;
import com.kratosgado.blog.dtos.request.CreateTagRequest;
import com.kratosgado.blog.dtos.request.PageRequest;
import com.kratosgado.blog.dtos.request.SearchPageRequest;
import com.kratosgado.blog.dtos.request.UpdateTagRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.ResponseDto;
import com.kratosgado.blog.dtos.response.TagResponse;
import com.kratosgado.blog.enums.UserRole;
import com.kratosgado.blog.backend.models.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tags")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Tags", description = "Tag management APIs")
public class TagController {

  private final TagService tagService;

  public TagController(TagService tagService) {
    this.tagService = tagService;
  }

  @PostMapping
  @SecuredCreateEndpoint(
      summary = "Create a new tag",
      description = "Creates a new tag. Requires authentication.",
      roles = {UserRole.AUTHOR, UserRole.ADMIN})
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseDto<Tag> createTag(
      @Valid @RequestBody @Parameter(description = "Tag creation request")
          CreateTagRequest request) {
    Tag tag = tagService.createTag(request);
    return ResponseDto.success(HttpStatus.CREATED.value(), "Tag created successfully", tag);
  }

  @PutMapping("/{id}")
  @SecuredUpdateEndpoint(
      summary = "Update a tag",
      description = "Updates an existing tag. Requires authentication.",
      roles = {UserRole.AUTHOR, UserRole.ADMIN})
  public ResponseDto<Tag> updateTag(
      @PathVariable @Parameter(description = "Tag ID") Long id,
      @Valid @RequestBody @Parameter(description = "Tag update request") UpdateTagRequest request) {
    Tag tag = tagService.updateTag(id, request);
    return ResponseDto.success("Tag updated successfully", tag);
  }

  @DeleteMapping("/{id}")
  @DeleteEndpoint(
      summary = "Delete a tag",
      description = "Deletes a tag by ID. Requires authentication.",
      roles = {UserRole.AUTHOR, UserRole.ADMIN})
  public ResponseDto<Void> deleteTag(@PathVariable @Parameter(description = "Tag ID") Long id) {
    tagService.deleteTag(id);
    return ResponseDto.success("Tag deleted successfully", null);
  }

  @GetMapping("/{id}")
  @GetEndpoint(
      summary = "Get a tag by ID",
      description = "Retrieves a single tag by its ID. Public access.")
  public ResponseDto<Tag> getTag(@PathVariable @Parameter(description = "Tag ID") Long id) {
    Tag tag = tagService.getTagById(id);
    return ResponseDto.success(tag);
  }

  @GetMapping("/slug/{slug}")
  @GetEndpoint(
      summary = "Get a tag by slug",
      description = "Retrieves a single tag by its slug. Public access.")
  public ResponseDto<Tag> getTagBySlug(
      @PathVariable @Parameter(description = "Tag slug") String slug) {
    Tag tag = tagService.getTagBySlug(slug);
    return ResponseDto.success(tag);
  }

  @GetMapping
  @GetEndpoint(
      summary = "Get all tags",
      description = "Retrieves a paginated list of all tags. Public access.")
  public ResponseDto<PageResponse<Tag>> getTags(@ParameterObject PageRequest page) {
    PageResponse<Tag> tags = tagService.getAllTags(page);
    return ResponseDto.success(tags);
  }

  @GetMapping("/search")
  @GetEndpoint(summary = "Search tags", description = "Searches for tags by keyword in name")
  public ResponseDto<PageResponse<Tag>> searchTags(@ParameterObject SearchPageRequest request) {
    PageResponse<Tag> tags = tagService.searchTags(request.getKeyword(), request);
    return ResponseDto.success(tags);
  }

  @GetMapping("/with-post-count")
  @GetEndpoint(
      summary = "Get all tags with post counts",
      description =
          "Retrieves a list of all tags including the number of posts for each. Public access.")
  public ResponseDto<List<TagResponse>> getTagsWithPostCount() {
    List<TagResponse> tags = tagService.getAllTagsWithPostCount();
    return ResponseDto.success(tags);
  }
}
