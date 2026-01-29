package com.kratosgado.blog.backend.controllers;

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

import com.kratosgado.blog.backend.annotations.OpenApi.DeleteEndpoint;
import com.kratosgado.blog.backend.annotations.OpenApi.GetEnpoint;
import com.kratosgado.blog.backend.annotations.OpenApi.SecuredCreateEndpoint;
import com.kratosgado.blog.backend.annotations.OpenApi.SecuredUpdateEndpoint;
import com.kratosgado.blog.backend.services.TagService;
import com.kratosgado.blog.dtos.request.CreateTagRequest;
import com.kratosgado.blog.dtos.request.PageRequest;
import com.kratosgado.blog.dtos.request.SearchPageRequest;
import com.kratosgado.blog.dtos.request.UpdateTagRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.models.Tag;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/tags")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Tags", description = "Tag management APIs")
public class TagController {

  private final TagService tagService;

  public TagController(TagService tagService) {
    this.tagService = tagService;
  }

  @PostMapping
  @Operation(summary = "Create a new tag", description = "Creates a new tag. Requires authentication.", security = @SecurityRequirement(name = "bearer-jwt"))
  @SecuredCreateEndpoint
  @ResponseStatus(HttpStatus.CREATED)
  public Tag createTag(
      @Valid @RequestBody @Parameter(description = "Tag creation request") CreateTagRequest request) {
    return tagService.createTag(request);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update a tag", description = "Updates an existing tag. Requires authentication.", security = @SecurityRequirement(name = "bearer-jwt"))
  @SecuredUpdateEndpoint
  public Tag updateTag(
      @PathVariable @Parameter(description = "Tag ID") Long id,
      @Valid @RequestBody @Parameter(description = "Tag update request") UpdateTagRequest request) {
    return tagService.updateTag(id, request);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a tag", description = "Deletes a tag by ID. Requires authentication.", security = @SecurityRequirement(name = "bearer-jwt"))
  @DeleteEndpoint
  public void deleteTag(
      @PathVariable @Parameter(description = "Tag ID") Long id) {
    tagService.deleteTag(id);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a tag by ID", description = "Retrieves a single tag by its ID. Public access.")
  @GetEnpoint
  public Tag getTag(
      @PathVariable @Parameter(description = "Tag ID") Long id) {
    return tagService.getTagById(id);
  }

  @GetMapping("/slug/{slug}")
  @Operation(summary = "Get a tag by slug", description = "Retrieves a single tag by its slug. Public access.")
  @GetEnpoint
  public Tag getTagBySlug(
      @PathVariable @Parameter(description = "Tag slug") String slug) {
    return tagService.getTagBySlug(slug);
  }

  @GetMapping
  @Operation(summary = "Get all tags", description = "Retrieves a paginated list of all tags. Public access.")
  @GetEnpoint
  public PageResponse<Tag> getTags(@ParameterObject PageRequest page) {
    return tagService.getAllTags(page);
  }

  @GetMapping("/search")
  @Operation(summary = "Search tags", description = "Searches for tags by keyword in name")
  @GetEnpoint
  public PageResponse<Tag> searchTags(@ParameterObject SearchPageRequest request) {
    return tagService.searchTags(request.getKeyword(), request);
  }

  @GetMapping("/with-post-count")
  @GetEnpoint
  @Operation(summary = "Get all tags with post counts", description = "Retrieves a list of all tags including the number of posts for each. Public access.")
  public List<com.kratosgado.blog.dtos.response.TagResponse> getTagsWithPostCount() {
    return tagService.getAllTagsWithPostCount();
  }
}

