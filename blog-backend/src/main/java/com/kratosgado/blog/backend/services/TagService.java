package com.kratosgado.blog.backend.services;

import com.kratosgado.blog.backend.models.Tag;
import com.kratosgado.blog.dtos.request.CreateTagRequest;
import com.kratosgado.blog.dtos.request.PageRequest;
import com.kratosgado.blog.dtos.request.UpdateTagRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.TagResponse;
import java.util.List;

public interface TagService {

  Tag createTag(CreateTagRequest request);

  Tag updateTag(Long id, UpdateTagRequest request);

  void deleteTag(Long id);

  Tag getTagById(Long id);

  Tag getTagBySlug(String slug);

  List<TagResponse> getAllTagsWithPostCount();

  PageResponse<Tag> getAllTags(PageRequest pageRequest);

  PageResponse<Tag> searchTags(String keyword, PageRequest pageRequest);
}
