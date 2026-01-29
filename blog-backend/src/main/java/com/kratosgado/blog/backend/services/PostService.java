package com.kratosgado.blog.backend.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.jpa.CategoryRepository;
import com.kratosgado.blog.backend.repositories.jpa.PostRepository;
import com.kratosgado.blog.backend.repositories.jpa.TagRepository;
import com.kratosgado.blog.backend.utils.BlogUtils;
import com.kratosgado.blog.backend.utils.DtoMapper;
import com.kratosgado.blog.dtos.request.CreatePostRequest;
import com.kratosgado.blog.dtos.request.UpdatePostRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.PostResponse;
import com.kratosgado.blog.enums.PostStatus;
import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.models.Tag;
import com.kratosgado.blog.models.User;

@Service
@Transactional(readOnly = true)
public class PostService {
  private final PostRepository postRepository;
  private final TagRepository tagRepository;
  private final CategoryRepository categoryRepository;

  public PostService(PostRepository postRepository, TagRepository tagRepository, CategoryRepository categoryRepository) {
    this.postRepository = postRepository;
    this.tagRepository = tagRepository;
    this.categoryRepository = categoryRepository;
  }

  @Transactional
  @CacheEvict(value = "posts", allEntries = true)
  public PostResponse createPost(CreatePostRequest request, User user) {
    Post post = new Post();
    post.setUser(user);
    post.setTitle(request.title());
    post.setSlug(BlogUtils.toSlug(request.title()));
    post.setContent(request.content());
    post.setExcerpt(request.excerpt());
    
    if (request.categoryId() != null) {
      post.setCategory(categoryRepository.findById(request.categoryId())
          .orElseThrow(() -> BlogException.badRequest("Category not found")));
    }
    
    post.setCoverImage(request.coverImage());
    post.setStatus(PostStatus.valueOf(request.status().toLowerCase()));

    if (request.tagIds() != null && request.tagIds().length > 0) {
      List<Tag> tags = tagRepository.findAllById(List.of(request.tagIds()));
      post.setTags(tags);
    }

    Post savedPost = postRepository.save(post);
    return DtoMapper.toPostResponse(savedPost);
  }

  @Transactional
  @Caching(evict = {
      @CacheEvict(value = "posts", key = "#result.slug"),
      @CacheEvict(value = "posts", allEntries = true)
  })
  public PostResponse updatePost(Long postId, UpdatePostRequest request, Long userId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> BlogException.notFound("Post not found"));

    if (!post.getUser().getId().equals(userId)) {
      throw BlogException.forbidden("You don't have permission to update this post");
    }

    if (request.title() != null) {
      post.setTitle(request.title());
      post.setSlug(BlogUtils.toSlug(request.title()));
    }
    if (request.content() != null)
      post.setContent(request.content());
    if (request.excerpt() != null)
      post.setExcerpt(request.excerpt());
    if (request.categoryId() != null) {
      post.setCategory(categoryRepository.findById(request.categoryId())
          .orElseThrow(() -> BlogException.badRequest("Category not found")));
    }
    if (request.coverImage() != null)
      post.setCoverImage(request.coverImage());
    if (request.status() != null)
      post.setStatus(request.status());

    if (request.tagIds() != null) {
      List<Tag> tags = tagRepository.findAllById(List.of(request.tagIds()));
      post.setTags(tags);
    }

    Post updatedPost = postRepository.save(post);
    return DtoMapper.toPostResponse(updatedPost);
  }

  @Transactional
  @CacheEvict(value = "posts", allEntries = true)
  public void deletePost(Long postId, Long userId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> BlogException.notFound("Post not found"));

    if (!post.getUser().getId().equals(userId)) {
      throw BlogException.forbidden("You don't have permission to delete this post");
    }

    postRepository.delete(post);
  }

  @Cacheable(value = "posts", key = "#slug")
  public PostResponse getPostBySlug(String slug) {
    Post post = postRepository.findBySlug(slug)
        .orElseThrow(() -> BlogException.notFound("Post not found"));
    return DtoMapper.toPostResponse(post);
  }

  public PostResponse getPostById(Long postId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> BlogException.notFound("Post not found"));
    return DtoMapper.toPostResponse(post);
  }

  @Transactional
  @CacheEvict(value = "posts", allEntries = true)
  public PostResponse publishPost(Long postId, Long userId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> BlogException.notFound("Post not found"));

    if (!post.getUser().getId().equals(userId)) {
      throw BlogException.forbidden("You don't have permission to publish this post");
    }

    post.setStatus(PostStatus.published);
    Post updatedPost = postRepository.save(post);
    return DtoMapper.toPostResponse(updatedPost);
  }

  public PageResponse<PostResponse> getPublishedPosts(com.kratosgado.blog.dtos.request.PageRequest pageRequest) {
    Pageable pageable = toPageable(pageRequest);
    Page<Post> postsPage = postRepository.findByStatus(PostStatus.published, pageable);
    return toPageResponse(postsPage);
  }

  public PageResponse<PostResponse> searchPosts(String keyword, com.kratosgado.blog.dtos.request.PageRequest pageRequest) {
    Pageable pageable = toPageable(pageRequest);
    Page<Post> postsPage = postRepository.searchPublishedPosts(keyword, pageable);
    return toPageResponse(postsPage);
  }

  public PageResponse<PostResponse> getUserPosts(Long userId, com.kratosgado.blog.dtos.request.PageRequest pageRequest) {
    Pageable pageable = toPageable(pageRequest);
    Page<Post> postsPage = postRepository.findByUserId(userId, pageable);
    return toPageResponse(postsPage);
  }

  public PageResponse<PostResponse> getPostsByCategory(Long categoryId, com.kratosgado.blog.dtos.request.PageRequest pageRequest) {
    Pageable pageable = toPageable(pageRequest);
    Page<Post> postsPage = postRepository.findByCategoryId(categoryId, pageable);
    return toPageResponse(postsPage);
  }

  private Pageable toPageable(com.kratosgado.blog.dtos.request.PageRequest pageRequest) {
    Sort sort = Sort.by(Sort.Direction.fromString(pageRequest.getSortDir()), pageRequest.getSortBy());
    return PageRequest.of(pageRequest.getPage(), pageRequest.getSize(), sort);
  }

  private PageResponse<PostResponse> toPageResponse(Page<Post> page) {
    List<PostResponse> content = page.getContent().stream()
        .map(DtoMapper::toPostResponse)
        .collect(Collectors.toList());
    
    return new PageResponse<>(
        content,
        page.getNumber(),
        page.getSize(),
        (int) page.getTotalElements(),
        page.getTotalPages(),
        page.isFirst(),
        page.isLast()
    );
  }
}
