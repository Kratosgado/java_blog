package com.kratosgado.blog.backend.cache;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.kratosgado.blog.backend.dao.CategoryDAO;
import com.kratosgado.blog.backend.dao.PostDAO;
import com.kratosgado.blog.backend.dao.TagDAO;
import com.kratosgado.blog.backend.dao.nosql.CommentMongoDAO;
import com.kratosgado.blog.backend.utils.DtoMapper;
import com.kratosgado.blog.dtos.response.PostResponse;
import com.kratosgado.blog.models.Category;
import com.kratosgado.blog.models.Comment;
import com.kratosgado.blog.models.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuration class for initializing and managing application caches.
 * Provides beans for different entity caches with automatic refresh
 * capabilities.
 */
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class CacheConfig {

  private final PostDAO postDAO;
  private final CategoryDAO categoryDAO;
  private final TagDAO tagDAO;
  private final CommentMongoDAO commentDAO;

  // Cache TTL constants (in milliseconds)
  private static final long POST_CACHE_TTL = 5 * 60 * 1000; // 5 minutes
  private static final long CATEGORY_CACHE_TTL = 30 * 60 * 1000; // 30 minutes
  private static final long TAG_CACHE_TTL = 30 * 60 * 1000; // 30 minutes
  private static final long COMMENT_CACHE_TTL = 2 * 60 * 1000; // 2 minutes

  /**
   * Post cache bean with key as Post ID.
   * Caches PostResponse objects for quick retrieval.
   */
  @Bean
  public PostCache postCache() {
    log.info("Initializing Post cache");
    PostCache cache = new PostCache(
        "PostCache",
        this::loadAllPosts,
        POST_CACHE_TTL);

    // Load initial data
    loadPostCache(cache);
    return cache;
  }

  /**
   * Category cache bean with key as Category ID.
   */
  @Bean
  public CategoryCache categoryCache() {
    log.info("Initializing Category cache");
    CategoryCache cache = new CategoryCache(
        "CategoryCache",
        () -> categoryDAO.getAllCategories(),
        CATEGORY_CACHE_TTL);

    // Load initial data
    loadCategoryCache(cache);
    return cache;
  }

  /**
   * Tag cache bean with key as Tag ID.
   */
  @Bean
  public TagCache tagCache() {
    log.info("Initializing Tag cache");
    TagCache cache = new TagCache(
        "TagCache",
        () -> tagDAO.getAllTags(),
        TAG_CACHE_TTL);

    // Load initial data
    loadTagCache(cache);
    return cache;
  }

  /**
   * Comment cache bean with key as Comment ID.
   */
  @Bean
  public CommentCache commentCache() {
    log.info("Initializing Comment cache");
    CommentCache cache = new CommentCache(
        "CommentCache",
        () -> commentDAO.getAllComments(),
        COMMENT_CACHE_TTL);

    // Load initial data
    loadCommentCache(cache);
    return cache;
  }

  /**
   * Load all posts with full details (author, category, tags)
   */
  private List<PostResponse> loadAllPosts() {
    return postDAO.getAllPosts().stream()
        .map(post -> {
          List<Tag> tags = tagDAO.getTagsByPostId(post.getId());
          return DtoMapper.toPostResponse(post, tags);
        })
        .toList();
  }

  /**
   * Load initial data into post cache.
   */
  private void loadPostCache(PostCache cache) {
    try {
      List<PostResponse> posts = loadAllPosts();

      posts.forEach(post -> cache.put(post.slug(), post));
      log.info("Loaded {} posts into cache", posts.size());
    } catch (Exception e) {
      log.error("Error loading post cache", e);
    }
  }

  /**
   * Load initial data into category cache.
   */
  private void loadCategoryCache(CategoryCache cache) {
    try {
      List<Category> categories = categoryDAO.getAllCategories();
      categories.forEach(category -> cache.put(category.getId().longValue(), category));
      log.info("Loaded {} categories into cache", categories.size());
    } catch (Exception e) {
      log.error("Error loading category cache", e);
    }
  }

  /**
   * Load initial data into tag cache.
   */
  private void loadTagCache(TagCache cache) {
    try {
      List<Tag> tags = tagDAO.getAllTags();
      tags.forEach(tag -> cache.put(tag.getId().longValue(), tag));
      log.info("Loaded {} tags into cache", tags.size());
    } catch (Exception e) {
      log.error("Error loading tag cache", e);
    }
  }

  /**
   * Load initial data into comment cache.
   */
  private void loadCommentCache(CommentCache cache) {
    try {
      List<Comment> comments = commentDAO.getAllComments();

      comments.forEach(comment -> cache.put(comment.getId(), comment));
      log.info("Loaded {} comments into cache", comments.size());
    } catch (Exception e) {
      log.error("Error loading comment cache", e);
    }
  }

  /**
   * Scheduled task to refresh post cache every 5 minutes.
   */
  @Scheduled(fixedRate = POST_CACHE_TTL)
  public void refreshPostCache() {
    log.debug("Scheduled post cache refresh triggered");
    PostCache cache = postCache();
    cache.clear();
    loadPostCache(cache);
  }

  /**
   * Scheduled task to refresh category cache every 30 minutes.
   */
  @Scheduled(fixedRate = CATEGORY_CACHE_TTL)
  public void refreshCategoryCache() {
    log.debug("Scheduled category cache refresh triggered");
    CategoryCache cache = categoryCache();
    cache.clear();
    loadCategoryCache(cache);
  }

  /**
   * Scheduled task to refresh tag cache every 30 minutes.
   */
  @Scheduled(fixedRate = TAG_CACHE_TTL)
  public void refreshTagCache() {
    log.debug("Scheduled tag cache refresh triggered");
    TagCache cache = tagCache();
    cache.clear();
    loadTagCache(cache);
  }

  /**
   * Scheduled task to refresh comment cache every 2 minutes.
   */
  @Scheduled(fixedRate = COMMENT_CACHE_TTL)
  public void refreshCommentCache() {
    log.debug("Scheduled comment cache refresh triggered");
    CommentCache cache = commentCache();
    cache.clear();
    loadCommentCache(cache);
  }

  /**
   * Specific cache implementations
   */
  public static class PostCache extends ConcurrentMapCache<String, PostResponse> {
    public PostCache(String cacheName, java.util.function.Supplier<List<PostResponse>> dataLoader, long ttlMillis) {
      super(cacheName, dataLoader, ttlMillis);
    }
  }

  public static class CategoryCache extends ConcurrentMapCache<Long, Category> {
    public CategoryCache(String cacheName, java.util.function.Supplier<List<Category>> dataLoader, long ttlMillis) {
      super(cacheName, dataLoader, ttlMillis);
    }
  }

  public static class TagCache extends ConcurrentMapCache<Long, Tag> {
    public TagCache(String cacheName, java.util.function.Supplier<List<Tag>> dataLoader, long ttlMillis) {
      super(cacheName, dataLoader, ttlMillis);
    }
  }

  public static class CommentCache extends ConcurrentMapCache<String, Comment> {
    public CommentCache(String cacheName, java.util.function.Supplier<List<Comment>> dataLoader, long ttlMillis) {
      super(cacheName, dataLoader, ttlMillis);
    }
  }
}
