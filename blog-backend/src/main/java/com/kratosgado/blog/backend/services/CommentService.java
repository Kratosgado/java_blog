package com.kratosgado.blog.backend.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kratosgado.blog.backend.repositories.jpa.CommentRepository;
import com.kratosgado.blog.dtos.request.CreateCommentRequest;
import com.kratosgado.blog.models.Comment;
import com.kratosgado.blog.models.CommentStatus;

@Service
public class CommentService {

  private static final Logger logger = LoggerFactory.getLogger(CommentService.class);

  private final CommentRepository commentRepository;

  public CommentService(CommentRepository commentRepository) {
    this.commentRepository = commentRepository;
  }

  @Transactional
  public Comment createComment(CreateCommentRequest request, Long userId) {
    Comment comment = new Comment(request.postId(), userId, request.content());
    comment.setStatus(CommentStatus.PENDING);

    comment = commentRepository.save(comment);
    logger.info("Comment created: {} on post {} by user {}", comment.getId(), request.postId(), userId);
    return comment;
  }

  @Transactional
  public Comment approveComment(Long commentId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new RuntimeException("Comment not found"));

    comment.setStatus(CommentStatus.APPROVED);
    comment = commentRepository.save(comment);

    logger.info("Comment approved: {}", commentId);
    return comment;
  }

  @Transactional
  public Comment rejectComment(Long commentId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new RuntimeException("Comment not found"));

    comment.setStatus(CommentStatus.REJECTED);
    comment = commentRepository.save(comment);

    logger.info("Comment rejected: {}", commentId);
    return comment;
  }

  @Transactional
  public void deleteComment(Long commentId, Long userId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new RuntimeException("Comment not found"));

    // Only the comment author can delete their comment
    if (!comment.getUserId().equals(userId)) {
      throw new RuntimeException("You don't have permission to delete this comment");
    }

    commentRepository.delete(comment);
    logger.info("Comment deleted: {} by user {}", commentId, userId);
  }

  public Page<Comment> getPostComments(Long postId, Pageable pageable) {
    return commentRepository.findByPostIdAndStatus(postId, CommentStatus.APPROVED, pageable);
  }

  public Page<Comment> getAllPostComments(Long postId, Pageable pageable) {
    return commentRepository.findByPostId(postId, pageable);
  }

  public Page<Comment> getUserComments(Long userId, Pageable pageable) {
    return commentRepository.findByUserId(userId, pageable);
  }

  public Long getPostCommentCount(Long postId) {
    return commentRepository.countByPostIdAndStatus(postId, CommentStatus.APPROVED);
  }
}
