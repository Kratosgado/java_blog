package com.kratosgado.blog.backend.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.jpa.PostRepository;
import com.kratosgado.blog.backend.utils.DtoMapper;
import com.kratosgado.blog.dtos.request.CreatePostRequest;
import com.kratosgado.blog.dtos.request.UpdatePostRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.PostResponse;
import com.kratosgado.blog.models.Post;

@Service
public class PostService {
  private final PostRepository postRepository;

  public PostService(PostRepository postRepository) {
    this.postRepository = postRepository;
  }

  @Transactional
  public PostResponse createPost(CreatePostRequest request, Long userId) {
    Post post = new Post();
    post.setUserId(userId);
    post.setTitle(request.title());
    post.setContent(request.content());
    post.setExcerpt(request.excerpt());
    post.setCategoryId(request.categoryId());
    post.setCoverImage(request.coverImage());
    post.setStatus(request.status());

    return DtoMapper.toPostResponse(postRepository.save(post));
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

    return DtoMapper.toPostResponse(postRepository.save(post));
  }

  @Transactional
  public void deletePost(Long postId, Long userId) {
    Post post = postRepository.findByIdAndUserId(postId, userId)
        .orElseThrow(() -> BlogException.notFound("Post not found or you don't have permission"));

    postRepository.delete(post);
  }

  public PostResponse getPostById(Long postId) {
    var post = postRepository.findById(postId)
        .orElseThrow(() -> BlogException.notFound("Post not found"));
    return DtoMapper.toPostResponse(post);
  }

  public PageResponse<PostResponse> getPublishedPosts(Pageable pageable) {
    final Page<Post> posts = postRepository.findPublishedPosts(pageable);
    var content = posts.getContent().stream().map(DtoMapper::toPostResponse).toList();

    return new PageResponse<>(content,
        pageable.getPageNumber() + 1,
        posts.getNumber(),
        posts.getTotalElements(),
        posts.getTotalPages(),
        posts.isFirst(),
        posts.isLast());
  }

  public PageResponse<PostResponse> searchPosts(String keyword, Pageable pageable) {
    final Page<Post> posts = postRepository.searchPublishedPosts(keyword, pageable);
    var content = posts.getContent().stream().map(DtoMapper::toPostResponse).toList();

    return new PageResponse<>(content,
        pageable.getPageNumber() + 1,
        posts.getNumber(),
        posts.getTotalElements(),
        posts.getTotalPages(),
        posts.isFirst(),
        posts.isLast());
  }

  public PageResponse<PostResponse> getUserPosts(Long userId, Pageable pageable) {
    final Page<Post> posts = postRepository.findByUserId(userId, pageable);
    var content = posts.getContent().stream().map(DtoMapper::toPostResponse).toList();

    return new PageResponse<>(content,
        pageable.getPageNumber() + 1,
        posts.getNumber(),
        posts.getTotalElements(),
        posts.getTotalPages(),
        posts.isFirst(),
        posts.isLast());
  }

  public PageResponse<PostResponse> getPostsByCategory(Long categoryId, Pageable pageable) {
    final Page<Post> posts = postRepository.findByCategoryId(categoryId, pageable);
    var content = posts.getContent().stream().map(DtoMapper::toPostResponse).toList();

    return new PageResponse<>(content,
        pageable.getPageNumber() + 1,
        posts.getNumber(),
        posts.getTotalElements(),
        posts.getTotalPages(),
        posts.isFirst(),
        posts.isLast());
  }

}
