package com.kratosgado.blog.backend.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kratosgado.blog.backend.repositories.jpa.PostRepository;
import com.kratosgado.blog.dtos.request.CreatePostRequest;
import com.kratosgado.blog.dtos.request.UpdatePostRequest;
import com.kratosgado.blog.models.Post;

@Service
public class PostService {

  private static final Logger logger = LoggerFactory.getLogger(PostService.class);

  private final PostRepository postRepository;

  public PostService(PostRepository postRepository) {
    this.postRepository = postRepository;
  }

  @Transactional
  public Post createPost(CreatePostRequest request, Long userId) {
    Post post = new Post();
    post.setUserId(userId);
    post.setTitle(request.title());
    post.setContent(request.content());
    post.setExcerpt(request.excerpt());
    post.setCategoryId(request.categoryId());
    post.setCoverImage(request.coverImage());
    post.setStatus(request.status());

    post = postRepository.save(post);
    logger.info("Post created: {} by user {}", post.getId(), userId);
    return post;
  }

  @Transactional
  public Post updatePost(Long postId, UpdatePostRequest request, Long userId) {
    Post post = postRepository.findByIdAndUserId(postId, userId)
        .orElseThrow(() -> new RuntimeException("Post not found or you don't have permission"));

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

    post = postRepository.save(post);
    logger.info("Post updated: {} by user {}", postId, userId);
    return post;
  }

  @Transactional
  public void deletePost(Long postId, Long userId) {
    Post post = postRepository.findByIdAndUserId(postId, userId)
        .orElseThrow(() -> new RuntimeException("Post not found or you don't have permission"));

    postRepository.delete(post);
    logger.info("Post deleted: {} by user {}", postId, userId);
  }

  public Post getPostById(Long postId) {
    return postRepository.findById(postId)
        .orElseThrow(() -> new RuntimeException("Post not found"));
  }

  public Page<Post> getPublishedPosts(Pageable pageable) {
    return postRepository.findPublishedPosts(pageable);
  }

  public Page<Post> searchPosts(String keyword, Pageable pageable) {
    return postRepository.searchPublishedPosts(keyword, pageable);
  }

  public Page<Post> getUserPosts(Long userId, Pageable pageable) {
    return postRepository.findByUserId(userId, pageable);
  }

  public Page<Post> getPostsByCategory(Long categoryId, Pageable pageable) {
    return postRepository.findByCategoryId(categoryId, pageable);
  }
}
