package com.kratosgado.blog.backend.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kratosgado.blog.backend.cache.CacheConfig.TagCache;
import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.jpa.TagRepository;
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

  @Transactional
  public Tag createTag(CreateTagRequest request) {

    String slug = generateSlug(request.name());

    if (tagRepository.existsBySlug(slug)) {
      throw BlogException.duplicateResource("Tag", "slug", slug);
    }

    Tag tag = new Tag(request.name(), slug, request.description());
    Tag savedTag = tagRepository.save(tag);

    // Add to cache
    tagCache.put(savedTag.getId(), savedTag);
    log.debug("Created tag with ID: {} and added to cache", savedTag.getId());

    return savedTag;
  }

  @Transactional
  public Tag updateTag(Long id, UpdateTagRequest request) {

    Tag tag = tagRepository.findById(id)
        .orElseThrow(() -> BlogException.notFound("Tag", "id", id));

    if (request.name() != null && !request.name().equals(tag.getName())) {
      String newSlug = generateSlug(request.name());
      if (tagRepository.existsBySlug(newSlug) && !newSlug.equals(tag.getSlug())) {
        throw BlogException.duplicateResource("Tag", "slug", newSlug);
      }
      tag.setName(request.name());
      tag.setSlug(newSlug);
    }

    if (request.description() != null) {
      tag.setDescription(request.description());
    }

    Tag updatedTag = tagRepository.save(tag);

    // Update cache
    tagCache.put(updatedTag.getId(), updatedTag);
    log.debug("Updated tag with ID: {} in cache", updatedTag.getId());

    return updatedTag;
  }

  @Transactional
  public void deleteTag(Long id) {

    if (!tagRepository.existsById(id)) {
      throw BlogException.notFound("Tag", "id", id);
    }

    tagRepository.deleteById(id);

    // Evict from cache
    tagCache.evict(id);
    log.debug("Deleted tag with ID: {} and evicted from cache", id);

  }

  public Tag getTagById(Long id) {

    // Try to get from cache first
    return tagCache.get(id).orElseGet(() -> {
      log.debug("Cache miss for tag ID: {}, fetching from database", id);
      Tag tag = tagRepository.findById(id)
          .orElseThrow(() -> BlogException.notFound("Tag", "id", id));
      
      // Add to cache
      tagCache.put(id, tag);
      return tag;
    });
  }

  public Tag getTagBySlug(String slug) {

    log.debug("Searching for tag by slug: {} in cache", slug);
    
    // Search cache for tag with matching slug
    return tagCache.getAll().stream()
        .filter(tag -> tag.getSlug().equals(slug))
        .findFirst()
        .orElseGet(() -> {
          log.debug("Cache miss for tag slug: {}, fetching from database", slug);
          Tag tag = tagRepository.findBySlug(slug)
              .orElseThrow(() -> BlogException.notFound("Tag", "slug", slug));
          
          // Add to cache
          tagCache.put(tag.getId(), tag);
          return tag;
        });
  }

  public PageResponse<Tag> getAllTags(Pageable pageable) {

    log.debug("Getting all tags from cache with pagination");
    
    String sortField = pageable.getSort().isSorted() 
        ? pageable.getSort().iterator().next().getProperty() 
        : "createdAt";
    boolean ascending = pageable.getSort().isSorted() 
        ? pageable.getSort().iterator().next().isAscending() 
        : false;

    return tagCache.paginate(
        pageable.getPageNumber(),
        pageable.getPageSize(),
        sortField,
        ascending
    );
  }

  public PageResponse<Tag> searchTags(String keyword, Pageable pageable) {

    log.debug("Searching tags in cache with keyword: '{}'", keyword);
    
    String sortField = pageable.getSort().isSorted() 
        ? pageable.getSort().iterator().next().getProperty() 
        : "createdAt";
    boolean ascending = pageable.getSort().isSorted() 
        ? pageable.getSort().iterator().next().isAscending() 
        : false;
    
    String lowerKeyword = keyword.toLowerCase();
    return tagCache.search(
        tag -> tag.getName().toLowerCase().contains(lowerKeyword) ||
               (tag.getDescription() != null && tag.getDescription().toLowerCase().contains(lowerKeyword)),
        pageable.getPageNumber(),
        pageable.getPageSize(),
        sortField,
        ascending
    );
  }

  private String generateSlug(String name) {
    return name.toLowerCase()
        .replaceAll("[^a-z0-9]+", "-")
        .replaceAll("^-+|-+$", "");
  }
}
