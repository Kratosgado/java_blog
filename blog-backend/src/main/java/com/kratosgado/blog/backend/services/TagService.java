package com.kratosgado.blog.backend.services;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.kratosgado.blog.backend.cache.CacheConfig.TagCache;
import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.jdbc.TagRepository;
import com.kratosgado.blog.backend.utils.BlogUtils;
import com.kratosgado.blog.backend.utils.DtoMapper;
import com.kratosgado.blog.dtos.request.CreateTagRequest;
import com.kratosgado.blog.dtos.request.PageRequest;
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

    return tagRepository.update(tag);
  }

  public void deleteTag(Long id) {
    tagRepository.deleteById(id);
  }

  public Tag getTagById(Long id) {
    // Try to get from cache first
    return tagCache.get(id).orElseGet(() -> {

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

  public java.util.List<com.kratosgado.blog.dtos.response.TagResponse> getAllTagsWithPostCount() {
    return tagRepository.findAllWithPostCount();
  }

  public PageResponse<Tag> getAllTags(PageRequest pageRequest) {
    var tags = tagRepository.findAll(pageRequest.getSize(), pageRequest.getOffset(), pageRequest.getSortBy(), pageRequest.getSortDir());
    long totalItems = tagRepository.count();
    return DtoMapper.toPageResponse(tags, pageRequest.getPage(), pageRequest.getSize(), (int) totalItems);
  }

  public PageResponse<Tag> searchTags(String keyword, PageRequest pageRequest) {
    var tags = tagRepository.searchByKeyword(keyword, pageRequest.getSize(), pageRequest.getOffset(), pageRequest.getSortBy(), pageRequest.getSortDir());
    long totalItems = tagRepository.countByKeyword(keyword);
    return DtoMapper.toPageResponse(tags, pageRequest.getPage(), pageRequest.getSize(), (int) totalItems);
  }
}
