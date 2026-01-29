package com.kratosgado.blog.backend.services;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.jpa.TagRepository;
import com.kratosgado.blog.backend.utils.BlogUtils;
import com.kratosgado.blog.backend.utils.DtoMapper;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.TagResponse;
import com.kratosgado.blog.models.Tag;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional(readOnly = true)
public class TagService {

  private final TagRepository tagRepository;

  public TagService(TagRepository tagRepository) {
    this.tagRepository = tagRepository;
  }

  @Transactional
  @CacheEvict(value = "tags", allEntries = true)
  public Tag createTag(com.kratosgado.blog.dtos.request.CreateTagRequest request) {
    String slug = BlogUtils.toSlug(request.name());

    if (tagRepository.findBySlug(slug).isPresent()) {
      throw BlogException.duplicateResource("Tag", "slug", slug);
    }
    Tag tag = new Tag(null, request.name(), slug, request.description());

    return tagRepository.save(tag);
  }

  @Transactional
  @CacheEvict(value = "tags", allEntries = true)
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

  @Transactional
  @CacheEvict(value = "tags", allEntries = true)
  public void deleteTag(Long id) {
    tagRepository.deleteById(id);
  }

  @Cacheable(value = "tags", key = "#id")
  public Tag getTagById(Long id) {
    return tagRepository.findById(id)
        .orElseThrow(() -> BlogException.notFound("Tag", "id", id));
  }

  @Cacheable(value = "tags", key = "#slug")
  public Tag getTagBySlug(String slug) {
    return tagRepository.findBySlug(slug)
        .orElseThrow(() -> BlogException.notFound("Tag", "slug", slug));
  }

  public List<TagResponse> getAllTagsWithPostCount() {
    return tagRepository.findAllWithPostCount();
  }

  public PageResponse<Tag> getAllTags(com.kratosgado.blog.dtos.request.PageRequest pageRequest) {
    Sort sort = Sort.by(Sort.Direction.fromString(pageRequest.getSortDir()), pageRequest.getSortBy());
    Pageable pageable = PageRequest.of(pageRequest.getPage(), pageRequest.getSize(), sort);
    Page<Tag> tagPage = tagRepository.findAll(pageable);
    
    return new PageResponse<>(
        tagPage.getContent(),
        tagPage.getNumber(),
        tagPage.getSize(),
        (int) tagPage.getTotalElements(),
        tagPage.getTotalPages(),
        tagPage.isFirst(),
        tagPage.isLast()
    );
  }

  public PageResponse<Tag> searchTags(String keyword, com.kratosgado.blog.dtos.request.PageRequest pageRequest) {
    Sort sort = Sort.by(Sort.Direction.fromString(pageRequest.getSortDir()), pageRequest.getSortBy());
    Pageable pageable = PageRequest.of(pageRequest.getPage(), pageRequest.getSize(), sort);
    Page<Tag> tagPage = tagRepository.searchByKeyword(keyword, pageable);
    
    return new PageResponse<>(
        tagPage.getContent(),
        tagPage.getNumber(),
        tagPage.getSize(),
        (int) tagPage.getTotalElements(),
        tagPage.getTotalPages(),
        tagPage.isFirst(),
        tagPage.isLast()
    );
  }
}
