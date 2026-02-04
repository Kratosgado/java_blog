package com.kratosgado.blog.backend.services;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.kratosgado.blog.dtos.request.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.jpa.TagRepository;
import com.kratosgado.blog.backend.utils.BlogConstants.CacheNames;
import com.kratosgado.blog.backend.utils.BlogUtils;
import com.kratosgado.blog.backend.utils.DtoMapper;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.TagResponse;
import com.kratosgado.blog.models.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
public class TagService {

  private final TagRepository tagRepository;

  @Transactional(isolation = Isolation.READ_COMMITTED)
  @Caching(put = @CachePut(value = CacheNames.TAGS, key = "#result.id"), evict = @CacheEvict(value = CacheNames.TAGLIST, allEntries = true))
  public Tag createTag(com.kratosgado.blog.dtos.request.CreateTagRequest request) {
    String slug = BlogUtils.toSlug(request.name());

    if (tagRepository.findBySlug(slug).isPresent()) {
      throw BlogException.duplicateResource("Tag", "slug", slug);
    }
    Tag tag = new Tag(null, request.name(), slug, request.description());

    return tagRepository.save(tag);
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  @Caching(put = @CachePut(value = CacheNames.TAGS, key = "#result.id"), evict = @CacheEvict(value = CacheNames.TAGLIST, allEntries = true))
  public Tag updateTag(Long id, com.kratosgado.blog.dtos.request.UpdateTagRequest request) {
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

  @Transactional(isolation = Isolation.READ_COMMITTED)
  @Caching(put = @CachePut(value = CacheNames.TAGS, key = "#result.id"), evict = @CacheEvict(value = CacheNames.TAGLIST, allEntries = true))
  public void deleteTag(Long id) {
    tagRepository.deleteById(id);
  }

  @Cacheable(value = CacheNames.TAGS, key = "#id")
  public Tag getTagById(Long id) {
    return tagRepository.findById(id)
        .orElseThrow(() -> BlogException.notFound("Tag", "id", id));
  }

  @Cacheable(value = CacheNames.TAGS, key = "#slug")
  public Tag getTagBySlug(String slug) {
    return tagRepository.findBySlug(slug)
        .orElseThrow(() -> BlogException.notFound("Tag", "slug", slug));
  }

  @Cacheable(value = CacheNames.TAGLIST, key = "'withPostCount'")
  public List<TagResponse> getAllTagsWithPostCount() {
    return tagRepository.findAllWithPostCount();
  }

  @Cacheable(value = CacheNames.TAGLIST, key = "'getAllTags-' + #pageRequest.toString()")
  public PageResponse<Tag> getAllTags(PageRequest pageRequest) {
    Pageable pageable = pageRequest.toPageable();
    Page<Tag> tagPage = tagRepository.findAll(pageable);

    return DtoMapper.toPageResponse(tagPage);
  }

  @Cacheable(value = CacheNames.TAGLIST, key = "'searchTags-' + #keyword + '-' + #pageRequest.toString()")
  public PageResponse<Tag> searchTags(String keyword, PageRequest pageRequest) {
    Pageable pageable = pageRequest.toPageable();
    Page<Tag> tagPage = tagRepository.searchByKeyword(keyword, pageable);

    return DtoMapper.toPageResponse(tagPage);
  }
}
