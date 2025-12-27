package com.kratosgado.blog.services;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.dao.TagDAO;
import com.kratosgado.blog.dtos.request.CreateTagDto;
import com.kratosgado.blog.models.Tag;
import com.kratosgado.blog.utils.exceptions.BlogExceptions;
import com.kratosgado.blog.utils.validators.Validator;

public class TagService {
  private static final Logger logger = LoggerFactory.getLogger(TagService.class);
  private final TagDAO tagDAO;

  public TagService() {
    this.tagDAO = new TagDAO();
  }

  public boolean createTag(CreateTagDto dto) {
    Validator.validate(dto);
    Tag tag = new Tag(dto);
    Optional<Tag> existing = tagDAO.getTagBySlug(tag.getSlug());
    if (existing.isPresent()) {
      throw BlogExceptions.conflict("Tag with this slug already exists");
    }
    return tagDAO.createTag(tag);
  }

  public boolean updateTag(int id, CreateTagDto dto) {
    Validator.validate(dto);
    Optional<Tag> existing = tagDAO.getTagById(id);
    if (existing.isEmpty()) {
      throw BlogExceptions.notFound("Tag not found");
    }
    Tag tag = new Tag(dto);
    tag.setId(id);
    return tagDAO.updateTag(tag);
  }

  public boolean deleteTag(int id) {
    Optional<Tag> tag = tagDAO.getTagById(id);
    if (tag.isEmpty()) {
      throw BlogExceptions.notFound("Tag not found");
    }
    return tagDAO.deleteTag(id);
  }

  public Optional<Tag> getTagById(int id) {
    return tagDAO.getTagById(id);
  }

  public Optional<Tag> getTagBySlug(String slug) {
    return tagDAO.getTagBySlug(slug);
  }

  public List<Tag> getAllTags() {
    return tagDAO.getAllTags();
  }

  public List<Tag> getTagsByPostId(int postId) {
    return tagDAO.getTagsByPostId(postId);
  }

  public boolean addTagToPost(int postId, int tagId) {
    return tagDAO.addTagToPost(postId, tagId);
  }

  public boolean removeTagFromPost(int postId, int tagId) {
    return tagDAO.removeTagFromPost(postId, tagId);
  }
}
