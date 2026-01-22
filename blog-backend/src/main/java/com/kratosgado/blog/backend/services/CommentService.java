package com.kratosgado.blog.backend.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kratosgado.blog.backend.cache.CacheConfig.CommentCache;
import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.mongo.CommentRepository;
import com.kratosgado.blog.backend.utils.DtoMapper;
import com.kratosgado.blog.dtos.request.CreateCommentRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.enums.CommentStatus;
import com.kratosgado.blog.models.Comment;
import com.kratosgado.blog.models.User;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CommentService {

  private final CommentRepository commentRepository;
  private final UserService userService;
  private final CommentCache commentCache;

  public CommentService(CommentRepository commentRepository, UserService userService, CommentCache commentCache) {
    this.commentRepository = commentRepository;
    this.userService = userService;
    this.commentCache = commentCache;
  }

  @Transactional
  public Comment createComment(CreateCommentRequest request, Long userId) {
    User user = userService.getUserById(userId);

    Comment comment = new Comment(request.postId(), userId, request.content());
    comment.setStatus(CommentStatus.pending);
    // Populate author snapshot
    comment.setAuthorName(user.getUsername());
    comment.setAuthorAvatarUrl(user.getAvatarUrl());
    Comment saved = commentRepository.save(comment);
    
    // Add to cache
    commentCache.put(saved.getId(), saved);
    log.debug("Created comment with ID: {} and added to cache", saved.getId());

    return saved;
  }

  @Transactional
  public Comment approveComment(String commentId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> BlogException.notFound("Comment not found"));

    comment.setStatus(CommentStatus.approved);
    Comment updated = commentRepository.save(comment);
    
    // Update cache
    commentCache.put(updated.getId(), updated);
    log.debug("Approved comment with ID: {} and updated cache", updated.getId());

    return updated;
  }

  @Transactional
  public Comment rejectComment(String commentId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> BlogException.notFound("Comment not found"));

    comment.setStatus(CommentStatus.rejected);
    Comment updated = commentRepository.save(comment);
    
    // Update cache
    commentCache.put(updated.getId(), updated);
    log.debug("Rejected comment with ID: {} and updated cache", updated.getId());

    return updated;
  }

  @Transactional
  public void deleteComment(String commentId, Long userId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> BlogException.notFound("Comment not found"));

    // Only the comment author can delete their comment
    if (!comment.getUserId().equals(userId)) {
      throw BlogException.unauthorized("You are not allowed to delete this comment");
    }
    commentRepository.delete(comment);
    
    // Evict from cache
    commentCache.evict(commentId);
    log.debug("Deleted comment with ID: {} and evicted from cache", commentId);
  }

  public Comment getCommentById(String commentId) {
    // Try to get from cache first
    return commentCache.get(commentId).orElseGet(() -> {
      log.debug("Cache miss for comment ID: {}, fetching from database", commentId);
      Comment comment = commentRepository.findById(commentId)
          .orElseThrow(() -> BlogException.notFound("Comment not found"));
      
      // Add to cache
      commentCache.put(commentId, comment);
      return comment;
    });
  }

  public PageResponse<Comment> getPostComments(Long postId, Pageable pageable) {
    log.debug("Getting approved comments for post ID: {} from cache", postId);
    
    String sortField = pageable.getSort().isSorted() 
        ? pageable.getSort().iterator().next().getProperty() 
        : "createdAt";
    boolean ascending = pageable.getSort().isSorted() 
        ? pageable.getSort().iterator().next().isAscending() 
        : false;
    
    return commentCache.search(
        comment -> comment.getPostId().equals(postId) && 
                   comment.getStatus() == CommentStatus.approved,
        pageable.getPageNumber(),
        pageable.getPageSize(),
        sortField,
        ascending
    );
  }

  public PageResponse<Comment> getAllPostComments(Long postId, Pageable pageable) {
    log.debug("Getting all comments for post ID: {} from cache", postId);
    
    String sortField = pageable.getSort().isSorted() 
        ? pageable.getSort().iterator().next().getProperty() 
        : "createdAt";
    boolean ascending = pageable.getSort().isSorted() 
        ? pageable.getSort().iterator().next().isAscending() 
        : false;
    
    return commentCache.search(
        comment -> comment.getPostId().equals(postId),
        pageable.getPageNumber(),
        pageable.getPageSize(),
        sortField,
        ascending
    );
  }

  public PageResponse<Comment> getUserComments(Long userId, Pageable pageable) {
    log.debug("Getting comments for user ID: {} from cache", userId);
    
    String sortField = pageable.getSort().isSorted() 
        ? pageable.getSort().iterator().next().getProperty() 
        : "createdAt";
    boolean ascending = pageable.getSort().isSorted() 
        ? pageable.getSort().iterator().next().isAscending() 
        : false;
    
    return commentCache.search(
        comment -> comment.getUserId().equals(userId),
        pageable.getPageNumber(),
        pageable.getPageSize(),
        sortField,
        ascending
    );
  }

  public Long getPostCommentCount(Long postId) {
    log.debug("Counting approved comments for post ID: {} from cache", postId);
    
    return commentCache.getAll().stream()
        .filter(comment -> comment.getPostId().equals(postId) && 
                          comment.getStatus() == CommentStatus.approved)
        .count();
  }

}
