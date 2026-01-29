package com.kratosgado.blog.backend.graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.kratosgado.blog.backend.services.TagService;
import com.kratosgado.blog.dtos.request.PageRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.models.Tag;

@Controller
public class TagGraphQLController {

  private final TagService tagService;

  public TagGraphQLController(TagService tagService) {
    this.tagService = tagService;
  }

  @QueryMapping
  public Tag tag(@Argument Long id) {
    return tagService.getTagById(id);
  }

  @QueryMapping
  public PageResponse<Tag> tags(
      @Argument(name = "page") int page,
      @Argument(name = "size") int size) {
    return tagService.getAllTags(new PageRequest(page, size, "id", "desc"));
  }

  @QueryMapping
  public PageResponse<Tag> searchTags(
      @Argument String keyword,
      @Argument(name = "page") int page,
      @Argument(name = "size") int size) {
    return tagService.searchTags(keyword, new PageRequest(page, size, "id", "desc"));
  }
}
