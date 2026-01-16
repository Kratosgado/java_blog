package com.kratosgado.blog.backend.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kratosgado.blog.backend.exceptions.DuplicateResourceException;
import com.kratosgado.blog.backend.exceptions.ResourceNotFoundException;
import com.kratosgado.blog.backend.repositories.jpa.TagRepository;
import com.kratosgado.blog.dtos.request.CreateTagRequest;
import com.kratosgado.blog.dtos.request.UpdateTagRequest;
import com.kratosgado.blog.models.Tag;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TagService {

  private final TagRepository tagRepository;

  @Transactional
  public Tag createTag(CreateTagRequest request) {

    String slug = generateSlug(request.name());

    if (tagRepository.existsBySlug(slug)) {
      throw new DuplicateResourceException("Tag", "slug", slug);
    }

    Tag tag = new Tag(request.name(), slug, request.description());
    Tag savedTag = tagRepository.save(tag);

    return savedTag;
  }

  @Transactional
  public Tag updateTag(Long id, UpdateTagRequest request) {

    Tag tag = tagRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Tag", "id", id));

    if (request.name() != null && !request.name().equals(tag.getName())) {
      String newSlug = generateSlug(request.name());
      if (tagRepository.existsBySlug(newSlug) && !newSlug.equals(tag.getSlug())) {
        throw new DuplicateResourceException("Tag", "slug", newSlug);
      }
      tag.setName(request.name());
      tag.setSlug(newSlug);
    }

    if (request.description() != null) {
      tag.setDescription(request.description());
    }

    Tag updatedTag = tagRepository.save(tag);

    return updatedTag;
  }

  @Transactional
  public void deleteTag(Long id) {

    if (!tagRepository.existsById(id)) {
      throw new ResourceNotFoundException("Tag", "id", id);
    }

    tagRepository.deleteById(id);

  }

  public Tag getTagById(Long id) {

    return tagRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Tag", "id", id));
  }

  public Tag getTagBySlug(String slug) {

    return tagRepository.findBySlug(slug)
        .orElseThrow(() -> new ResourceNotFoundException("Tag", "slug", slug));
  }

  public Page<Tag> getAllTags(Pageable pageable) {

    return tagRepository.findAllOrderByCreatedAtDesc(pageable);
  }

  public Page<Tag> searchTags(String keyword, Pageable pageable) {

    return tagRepository.searchByName(keyword, pageable);
  }

  private String generateSlug(String name) {
    return name.toLowerCase()
        .replaceAll("[^a-z0-9]+", "-")
        .replaceAll("^-+|-+$", "");
  }
}
