package com.kratosgado.blog.backend.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kratosgado.blog.backend.cache.CacheConfig.PostCache;
import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.jpa.PostRepository;
import com.kratosgado.blog.backend.utils.DtoMapper;
import com.kratosgado.blog.dtos.request.CreatePostRequest;
import com.kratosgado.blog.dtos.request.UpdatePostRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.PostResponse;
import com.kratosgado.blog.enums.PostStatus;
import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.models.User;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PostService {
  private final PostRepository postRepository;
  private final PostCache postCache;

  public PostService(PostRepository postRepository, PostCache postCache) {
    this.postRepository = postRepository;
    this.postCache = postCache;
  }

  @Transactional
  public PostResponse createPost(CreatePostRequest request, User user) {
    Post post = new Post();
    post.setUser(user);
    post.setTitle(request.title());
    post.setContent(request.content());
    post.setExcerpt(request.excerpt());
    post.setCategoryId(request.categoryId());
    post.setCoverImage(request.coverImage());
    post.setStatus(request.status());

    PostResponse response = DtoMapper.toPostResponse(postRepository.save(post));
    
    // Add to cache
    postCache.put(response.id(), response);
    log.debug("Created post with ID: {} and added to cache", response.id());
    
    return response;
  }

  @Transactional
  public PostResponse updatePost(Long postId, UpdatePostRequest request, Long userId) {
    Post post = postRepository.findByIdAndUserId(postId, userId)
        .orElseThrow(() -> BlogException.notFound("Post not found or you don't have permission"));

    if (request.title() != null)
      post.setTitle(request.title());
    if (request.content() != null)
      post.setContent(request.content());
    if (request.excerpt() != null)
      post.setExcerpt(request.excerpt());
    if (request.categoryId() != null)
      post.setCategoryId(request.categoryId());
    if (request.coverImage() != null)
      post.setCoverImage(request.coverImage());
    if (request.status() != null)
      post.setStatus(request.status());

    PostResponse response = DtoMapper.toPostResponse(postRepository.save(post));
    
    // Update cache
    postCache.put(response.id(), response);
    log.debug("Updated post with ID: {} in cache", response.id());
    
    return response;
  }

  @Transactional
  public void deletePost(Long postId, Long userId) {
    Post post = postRepository.findByIdAndUserId(postId, userId)
        .orElseThrow(() -> BlogException.notFound("Post not found or you don't have permission"));

    postRepository.delete(post);
    
    // Evict from cache
    postCache.evict(postId);
    log.debug("Deleted post with ID: {} and evicted from cache", postId);
  }

  public PostResponse getPostById(Long postId) {
    // Try to get from cache first
    return postCache.get(postId).orElseGet(() -> {
      log.debug("Cache miss for post ID: {}, fetching from database", postId);
      var post = postRepository.findById(postId)
          .orElseThrow(() -> BlogException.notFound("Post not found"));
      PostResponse response = DtoMapper.toPostResponse(post);
      
      // Add to cache
      postCache.put(postId, response);
      return response;
    });
  }

  public PageResponse<PostResponse> getPublishedPosts(Pageable pageable) {
    log.debug("Getting published posts from cache - page: {}, size: {}", 
        pageable.getPageNumber(), pageable.getPageSize());
    
    // Use cache with search predicate for published posts
    String sortField = pageable.getSort().isSorted() 
        ? pageable.getSort().iterator().next().getProperty() 
        : "createdAt";
    boolean ascending = pageable.getSort().isSorted() 
        ? pageable.getSort().iterator().next().isAscending() 
        : false;
    
    return postCache.search(
        post -> post.status() == PostStatus.published,
        pageable.getPageNumber(),
        pageable.getPageSize(),
        sortField,
        ascending
    );
  }

  public PageResponse<PostResponse> searchPosts(String keyword, Pageable pageable) {
    log.debug("Searching posts in cache with keyword: '{}', page: {}, size: {}", 
        keyword, pageable.getPageNumber(), pageable.getPageSize());
    
    // Use cache with search predicate for keyword matching
    String sortField = pageable.getSort().isSorted() 
        ? pageable.getSort().iterator().next().getProperty() 
        : "createdAt";
    boolean ascending = pageable.getSort().isSorted() 
        ? pageable.getSort().iterator().next().isAscending() 
        : false;
    
    String lowerKeyword = keyword.toLowerCase();
    return postCache.search(
        post -> post.status() == PostStatus.published && 
                (post.title().toLowerCase().contains(lowerKeyword) || 
                 post.content().toLowerCase().contains(lowerKeyword) ||
                 post.excerpt().toLowerCase().contains(lowerKeyword)),
        pageable.getPageNumber(),
        pageable.getPageSize(),
        sortField,
        ascending
    );
  }

  public PageResponse<PostResponse> getUserPosts(Long userId, Pageable pageable) {
    log.debug("Getting posts for user ID: {} from cache", userId);
    
    String sortField = pageable.getSort().isSorted() 
        ? pageable.getSort().iterator().next().getProperty() 
        : "createdAt";
    boolean ascending = pageable.getSort().isSorted() 
        ? pageable.getSort().iterator().next().isAscending() 
        : false;
    
    return postCache.search(
        post -> post.author().id().equals(userId),
        pageable.getPageNumber(),
        pageable.getPageSize(),
        sortField,
        ascending
    );
  }

  public PageResponse<PostResponse> getPostsByCategory(Long categoryId, Pageable pageable) {
    log.debug("Getting posts for category ID: {} from cache", categoryId);
    
    String sortField = pageable.getSort().isSorted() 
        ? pageable.getSort().iterator().next().getProperty() 
        : "createdAt";
    boolean ascending = pageable.getSort().isSorted() 
        ? pageable.getSort().iterator().next().isAscending() 
        : false;
    
    return postCache.search(
        post -> post.category() != null && post.category().id().equals(categoryId),
        pageable.getPageNumber(),
        pageable.getPageSize(),
        sortField,
        ascending
    );
  }

}
