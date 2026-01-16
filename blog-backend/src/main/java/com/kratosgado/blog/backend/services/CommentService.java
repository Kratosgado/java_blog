package com.kratosgado.blog.backend.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.jpa.CommentRepository;
import com.kratosgado.blog.dtos.request.CreateCommentRequest;
import com.kratosgado.blog.models.Comment;
import com.kratosgado.blog.models.CommentStatus;

@Service
public class CommentService {

  private final CommentRepository commentRepository;

  public CommentService(CommentRepository commentRepository) {
    this.commentRepository = commentRepository;
  }

  @Transactional
  public Comment createComment(CreateCommentRequest request, Long userId) {
    Comment comment = new Comment(request.postId(), userId, request.content());
    comment.setStatus(CommentStatus.PENDING);

    comment = commentRepository.save(comment);

    return comment;
  }

  @Transactional
  public Comment approveComment(Long commentId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> BlogException.notFound("Comment not found"));

    comment.setStatus(CommentStatus.APPROVED);
    comment = commentRepository.save(comment);

    return comment;
  }

  @Transactional
  public Comment rejectComment(Long commentId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> BlogException.notFound("Comment not found"));

    comment.setStatus(CommentStatus.REJECTED);
    comment = commentRepository.save(comment);

    return comment;
  }

  @Transactional
  public void deleteComment(Long commentId, Long userId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> BlogException.notFound("Comment not found"));

    // Only the comment author can delete their comment
    if (!comment.getUserId().equals(userId)) {
      throw BlogException.unauthorized("You are not allowed to delete this comment");
    }

    commentRepository.delete(comment);

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
