package com.kratosgado.blog.backend.graphql;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.kratosgado.blog.backend.services.TagService;
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
  public List<Tag> tags() {
    return tagService.getAllTags(1, 100).content();
  }
}
