package com.kratosgado.blog.services;

import java.util.List;
import java.util.Optional;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.dao.PostDAO;
import com.kratosgado.blog.dtos.request.CreatePostDto;
import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.utils.exceptions.BlogExceptions;
import com.kratosgado.blog.utils.validators.ValidatorEngine;

public class PostService {
  private static final Logger logger = LoggerFactory.getLogger(PostService.class);
  private final PostDAO postDAO;

  @Inject
  public PostService(PostDAO postDAO) {
    this.postDAO = postDAO;
  }

  public Optional<Post> createPost(CreatePostDto dto) {
    ValidatorEngine.validate(dto);
    Post post = new Post(dto);
    if (postDAO.createPost(post)) {
      return Optional.of(post);
    }
    return Optional.empty();
  }

  public boolean updatePost(Post post) {
    Optional<Post> existing = postDAO.getPostById(post.getId());
    if (existing.isEmpty()) {
      throw BlogExceptions.notFound("Post not found");
    }
    return postDAO.updatePost(post);
  }

  public boolean deletePost(int id) {
    Optional<Post> post = postDAO.getPostById(id);
    if (post.isEmpty()) {
      throw BlogExceptions.notFound("Post not found");
    }
    return postDAO.deletePost(id);
  }

  public Optional<Post> getPostById(int id) {
    return postDAO.getPostById(id);
  }

  public List<Post> getPostsByUserId(int userId) {
    return postDAO.getPostsByUserId(userId);
  }

  public List<Post> getPublishedPosts() {
    return postDAO.getPostsByStatus("published");
  }

  public List<Post> getDraftPosts(int userId) {
    List<Post> allDrafts = postDAO.getPostsByStatus("draft");
    return allDrafts.stream()
        .filter(p -> p.getUserId() == userId)
        .toList();
  }

  public List<Post> getAllPosts() {
    return postDAO.getAllPosts();
  }

  public boolean publishPost(int postId) {
    Optional<Post> post = postDAO.getPostById(postId);
    if (post.isEmpty()) {
      throw BlogExceptions.notFound("Post not found");
    }
    Post p = post.get();
    p.setStatus("published");
    return postDAO.updatePost(p);
  }

  public boolean incrementViews(int postId) {
    return postDAO.incrementViews(postId);
  }

  public long getTotalViews(int userId) {
    return postDAO.getPostsByUserId(userId).stream()
        .mapToLong(Post::getViews)
        .sum();
  }
}
