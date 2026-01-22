package com.kratosgado.blog.backend.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kratosgado.blog.backend.cache.CacheConfig.TagCache;
import com.kratosgado.blog.backend.dao.TagDAO;
import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.dtos.request.CreateTagRequest;
import com.kratosgado.blog.dtos.request.UpdateTagRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.models.Tag;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TagService {

  private final TagDAO tagDAO;
  private final TagCache tagCache;

  public TagService(TagDAO tagDAO, TagCache tagCache) {
    this.tagDAO = tagDAO;
    this.tagCache = tagCache;
  }

  public Tag createTag(CreateTagRequest request) {

    String slug = generateSlug(request.name());

    if (tagDAO.getTagBySlug(slug).isPresent()) {
      throw BlogException.duplicateResource("Tag", "slug", slug);
    }

    Tag tag = new Tag(request.name(), slug, request.description());
    
    return tagDAO.createTag(tag)
        .orElseThrow(() -> BlogException.internal("Failed to create tag"));
  }

  public Tag updateTag(Long id, UpdateTagRequest request) {

    Tag tag = tagDAO.getTagById(id.intValue())
        .orElseThrow(() -> BlogException.notFound("Tag", "id", id));

    if (request.name() != null && !request.name().equals(tag.getName())) {
      String newSlug = generateSlug(request.name());
      if (tagDAO.getTagBySlug(newSlug).isPresent() && !newSlug.equals(tag.getSlug())) {
        throw BlogException.duplicateResource("Tag", "slug", newSlug);
      }
      tag.setName(request.name());
      tag.setSlug(newSlug);
    }

    if (request.description() != null) {
      tag.setDescription(request.description());
    }

    if (!tagDAO.updateTag(tag)) {
      throw BlogException.internal("Failed to update tag");
    }

    return tag;
  }

  public void deleteTag(Long id) {

    if (!tagDAO.getTagById(id.intValue()).isPresent()) {
      throw BlogException.notFound("Tag", "id", id);
    }

    if (!tagDAO.deleteTag(id.intValue())) {
      throw BlogException.internal("Failed to delete tag");
    }
  }

  public Tag getTagById(Long id) {
    // Try to get from cache first
    return tagCache.get(id).orElseGet(() -> {
      log.debug("Cache miss for tag ID: {}, fetching from database", id);
      
      Tag tag = tagDAO.getTagById(id.intValue())
          .orElseThrow(() -> BlogException.notFound("Tag", "id", id));
      
      // Cache the result
      tagCache.put(id, tag);
      
      return tag;
    });
  }

  public Tag getTagBySlug(String slug) {
    return tagDAO.getTagBySlug(slug)
        .orElseThrow(() -> BlogException.notFound("Tag", "slug", slug));
  }

  public List<Tag> getAllTags() {
    return tagDAO.getAllTags();
  }

  public PageResponse<Tag> getAllTags(int page, int size) {
    List<Tag> allTags = tagDAO.getAllTags();
    int totalElements = allTags.size();
    int totalPages = (int) Math.ceil((double) totalElements / size);
    
    int offset = (page - 1) * size;
    int endIndex = Math.min(offset + size, totalElements);
    
    List<Tag> pagedTags = allTags.subList(offset, endIndex);
    
    return new PageResponse<>(
        pagedTags,
        page,
        size,
        totalElements,
        totalPages,
        page < totalPages,
        page > 1
    );
  }

  public PageResponse<Tag> searchTags(String keyword, int page, int size) {
    List<Tag> allTags = tagDAO.getAllTags();
    
    // Filter tags by keyword
    List<Tag> filteredTags = allTags.stream()
        .filter(tag -> tag.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                      (tag.getDescription() != null && tag.getDescription().toLowerCase().contains(keyword.toLowerCase())))
        .toList();
    
    int totalElements = filteredTags.size();
    int totalPages = (int) Math.ceil((double) totalElements / size);
    
    int offset = (page - 1) * size;
    int endIndex = Math.min(offset + size, totalElements);
    
    List<Tag> pagedTags = filteredTags.subList(offset, endIndex);
    
    return new PageResponse<>(
        pagedTags,
        page,
        size,
        totalElements,
        totalPages,
        page < totalPages,
        page > 1
    );
  }

  private String generateSlug(String name) {
    return name.toLowerCase()
        .replaceAll("[^a-z0-9]+", "-")
        .replaceAll("^-+|-+$", "");
  }
}

