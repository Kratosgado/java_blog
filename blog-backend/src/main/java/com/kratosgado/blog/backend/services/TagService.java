package com.kratosgado.blog.backend.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.kratosgado.blog.backend.cache.CacheConfig.TagCache;
import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.jpa.TagRepository;
import com.kratosgado.blog.backend.utils.BlogUtils;
import com.kratosgado.blog.backend.utils.DtoMapper;
import com.kratosgado.blog.dtos.request.CreateTagRequest;
import com.kratosgado.blog.dtos.request.UpdateTagRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.models.Tag;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TagService {

  private final TagRepository tagRepository;
  private final TagCache tagCache;

  public TagService(TagRepository tagRepository, TagCache tagCache) {
    this.tagRepository = tagRepository;
    this.tagCache = tagCache;
  }

  public Tag createTag(CreateTagRequest request) {

    String slug = BlogUtils.toSlug(request.name());

    if (tagRepository.findBySlug(slug).isPresent()) {
      throw BlogException.duplicateResource("Tag", "slug", slug);
    }

    Tag tag = new Tag(request.name(), slug, request.description());

    return tagRepository.save(tag);
  }

  public Tag updateTag(Long id, UpdateTagRequest request) {

    Tag tag = tagRepository.findById(id)
        .orElseThrow(() -> BlogException.notFound("Tag", "id", id));

    if (request.name() != null && !request.name().equals(tag.getName())) {
      String newSlug = BlogUtils.toSlug(request.name());
      if (tagRepository.findBySlug(newSlug).isPresent() && !newSlug.equals(tag.getSlug())) {
        throw BlogException.duplicateResource("Tag", "slug", newSlug);
      }
      tag.setName(request.name());
      tag.setSlug(newSlug);
    }

    if (request.description() != null) {
      tag.setDescription(request.description());
    }

    return tagRepository.save(tag);
  }

  public void deleteTag(Long id) {

    if (!tagRepository.existsById(id)) {
      throw BlogException.notFound("Tag", "id", id);
    }

    tagRepository.deleteById(id);
  }

  public Tag getTagById(Long id) {
    // Try to get from cache first
    return tagCache.get(id).orElseGet(() -> {
      log.debug("Cache miss for tag ID: {}, fetching from database", id);

      Tag tag = tagRepository.findById(id)
          .orElseThrow(() -> BlogException.notFound("Tag", "id", id));

      // Cache the result
      tagCache.put(id, tag);

      return tag;
    });
  }

  public Tag getTagBySlug(String slug) {
    return tagRepository.findBySlug(slug)
        .orElseThrow(() -> BlogException.notFound("Tag", "slug", slug));
  }

  public PageResponse<Tag> getAllTags(Pageable pageable) {
    Page<Tag> tagPage = tagRepository.findAll(pageable);
    return DtoMapper.toPageResponse(tagPage, pageable);
  }

  public PageResponse<Tag> searchTags(String keyword, Pageable pageable) {
    Page<Tag> tagPage = tagRepository.searchByName(keyword, pageable);
    return DtoMapper.toPageResponse(tagPage, pageable);
  }
}
