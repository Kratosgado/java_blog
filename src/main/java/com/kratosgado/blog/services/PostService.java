package com.kratosgado.blog.services;

import java.util.List;
import java.util.Optional;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.dao.PostDAO;
import com.kratosgado.blog.dtos.request.CreatePostDto;
import com.kratosgado.blog.dtos.request.LikePostDto;
import com.kratosgado.blog.dtos.request.UnlikePostDto;
import com.kratosgado.blog.dtos.request.UpdatePostDto;
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

  public boolean updatePost(UpdatePostDto dto) {
    ValidatorEngine.validate(dto);
    Optional<Post> existing = postDAO.getPostById(dto.id());
    if (existing.isEmpty()) {
      throw BlogExceptions.notFound("Post not found");
    }
    
    Post post = existing.get();
    if (dto.title() != null) {
      post.setTitle(dto.title());
    }
    if (dto.content() != null) {
      post.setContent(dto.content());
    }
    if (dto.excerpt() != null) {
      post.setExcerpt(dto.excerpt());
    }
    if (dto.status() != null) {
      post.setStatus(dto.status());
    }
    if (dto.coverImage() != null) {
      post.setCoverImage(dto.coverImage());
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

  /**
   * Like a post - increments the like count
   * 
   * @param dto the like post DTO containing postId and userId
   * @return true if successful, false otherwise
   */
  public boolean likePost(LikePostDto dto) {
    ValidatorEngine.validate(dto);
    Optional<Post> post = postDAO.getPostById(dto.postId());
    if (post.isEmpty()) {
      throw BlogExceptions.notFound("Post not found");
    }
    
    boolean success = postDAO.incrementLikesCount(dto.postId());
    if (success) {
      logger.info("Post {} liked by user {}", dto.postId(), dto.userId());
    } else {
      logger.error("Failed to like post {} for user {}", dto.postId(), dto.userId());
    }
    return success;
  }

  /**
   * Unlike a post - decrements the like count
   * 
   * @param dto the unlike post DTO containing postId and userId
   * @return true if successful, false otherwise
   */
  public boolean unlikePost(UnlikePostDto dto) {
    ValidatorEngine.validate(dto);
    Optional<Post> post = postDAO.getPostById(dto.postId());
    if (post.isEmpty()) {
      throw BlogExceptions.notFound("Post not found");
    }
    
    boolean success = postDAO.decrementLikesCount(dto.postId());
    if (success) {
      logger.info("Post {} unliked by user {}", dto.postId(), dto.userId());
    } else {
      logger.error("Failed to unlike post {} for user {}", dto.postId(), dto.userId());
    }
    return success;
  }

  /**
   * Get the current likes count for a post
   * 
   * @param postId the post ID
   * @return the number of likes
   */
  public int getLikesCount(int postId) {
    Optional<Post> post = postDAO.getPostById(postId);
    return post.map(Post::getLikesCount).orElse(0);
  }
  
  /**
   * Search posts by keyword (searches in title and content)
   * 
   * @param keyword the search keyword
   * @return list of matching posts
   */
  public List<Post> searchPostsByKeyword(String keyword) {
    if (keyword == null || keyword.trim().isEmpty()) {
      return getPublishedPosts();
    }
    return postDAO.searchPostsByKeyword(keyword.trim());
  }
  
  /**
   * Get posts by tag name
   * 
   * @param tagName the tag name
   * @return list of posts with the specified tag
   */
  public List<Post> getPostsByTag(String tagName) {
    if (tagName == null || tagName.trim().isEmpty()) {
      return getPublishedPosts();
    }
    return postDAO.getPostsByTag(tagName.trim());
  }
  
  /**
   * Get posts by category name
   * 
   * @param categoryName the category name
   * @return list of posts in the specified category
   */
  public List<Post> getPostsByCategory(String categoryName) {
    if (categoryName == null || categoryName.trim().isEmpty()) {
      return getPublishedPosts();
    }
    return postDAO.getPostsByCategory(categoryName.trim());
  }
  
  /**
   * Get posts by category ID
   * 
   * @param categoryId the category ID
   * @return list of posts in the specified category
   */
  public List<Post> getPostsByCategoryId(int categoryId) {
    return postDAO.getPostsByCategoryId(categoryId);
  }
  
  /**
   * Search posts by author name
   * 
   * @param authorName the author's name
   * @return list of posts by the specified author
   */
  public List<Post> searchPostsByAuthor(String authorName) {
    if (authorName == null || authorName.trim().isEmpty()) {
      return getPublishedPosts();
    }
    return postDAO.searchPostsByAuthor(authorName.trim());
  }
}
