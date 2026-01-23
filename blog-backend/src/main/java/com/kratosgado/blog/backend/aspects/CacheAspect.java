package com.kratosgado.blog.backend.aspects;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.kratosgado.blog.backend.cache.CacheConfig.CategoryCache;
import com.kratosgado.blog.backend.cache.CacheConfig.CommentCache;
import com.kratosgado.blog.backend.cache.CacheConfig.PostCache;
import com.kratosgado.blog.backend.cache.CacheConfig.TagCache;
import com.kratosgado.blog.dtos.response.PostResponse;
import com.kratosgado.blog.models.Category;
import com.kratosgado.blog.models.Comment;
import com.kratosgado.blog.models.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Aspect for automatic cache management.
 * Provides cross-cutting concerns for cache eviction and updates.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class CacheAspect {

  private final PostCache postCache;
  private final CategoryCache categoryCache;
  private final TagCache tagCache;
  private final CommentCache commentCache;

  /**
   * After updating a post, refresh it in cache.
   */
  @AfterReturning(pointcut = "execution(* com.kratosgado.blog.backend.services.PostService.updatePost(..))", returning = "result")
  public void afterUpdatePost(PostResponse result) {
    log.debug("Aspect: Updating post in cache: {}", result.id());
    postCache.updateIfPresent(result.slug(), result);
  }

  /**
   * After deleting a post, evict it from cache.
   */
  @AfterReturning(pointcut = "execution(* com.kratosgado.blog.backend.services.PostService.deletePost(..)) && args(postId,..)", argNames = "postId")
  public void afterDeletePost(String slug) {
    log.debug("Aspect: Evicting deleted post from cache: {}", slug);
    postCache.evict(slug);
  }

  /**
   * After updating a category, refresh it in cache.
   */
  @AfterReturning(pointcut = "execution(* com.kratosgado.blog.backend.services.CategoryService.updateCategory(..))", returning = "result")
  public void afterUpdateCategory(Category result) {
    log.debug("Aspect: Updating category in cache: {}", result.getId());
    categoryCache.put(result.getId().longValue(), result);
  }

  /**
   * After deleting a category, evict it from cache.
   */
  @AfterReturning(pointcut = "execution(* com.kratosgado.blog.backend.services.CategoryService.deleteCategory(..)) && args(categoryId)", argNames = "categoryId")
  public void afterDeleteCategory(Long categoryId) {
    log.debug("Aspect: Evicting deleted category from cache: {}", categoryId);
    categoryCache.evict(categoryId);
  }

  /**
   * After updating a tag, refresh it in cache.
   */
  @AfterReturning(pointcut = "execution(* com.kratosgado.blog.backend.services.TagService.updateTag(..))", returning = "result")
  public void afterUpdateTag(Tag result) {
    log.debug("Aspect: Updating tag in cache: {}", result.getId());
    tagCache.put(result.getId().longValue(), result);
  }

  /**
   * After deleting a tag, evict it from cache.
   */
  @AfterReturning(pointcut = "execution(* com.kratosgado.blog.backend.services.TagService.deleteTag(..)) && args(tagId)", argNames = "tagId")
  public void afterDeleteTag(Long tagId) {
    log.debug("Aspect: Evicting deleted tag from cache: {}", tagId);
    tagCache.evict(tagId);
  }

  /**
   * After approving a comment, refresh it in cache.
   */
  @AfterReturning(pointcut = "execution(* com.kratosgado.blog.backend.services.CommentService.approveComment(..))", returning = "result")
  public void afterApproveComment(Comment result) {
    log.debug("Aspect: Updating approved comment in cache: {}", result.getId());
    commentCache.put(result.getId(), result);
  }

  /**
   * After rejecting a comment, refresh it in cache.
   */
  @AfterReturning(pointcut = "execution(* com.kratosgado.blog.backend.services.CommentService.rejectComment(..))", returning = "result")
  public void afterRejectComment(Comment result) {
    log.debug("Aspect: Updating rejected comment in cache: {}", result.getId());
    commentCache.evict(result.getId());
  }

  /**
   * After deleting a comment, evict it from cache.
   */
  @AfterReturning(pointcut = "execution(* com.kratosgado.blog.backend.services.CommentService.deleteComment(..)) && args(commentId,..)", argNames = "commentId")
  public void afterDeleteComment(String commentId) {
    log.debug("Aspect: Evicting deleted comment from cache: {}", commentId);
    commentCache.evict(commentId);
  }
}
