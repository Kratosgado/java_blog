package com.kratosgado.blog.backend.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.mongo.CommentRepository;
import com.kratosgado.blog.backend.utils.DtoMapper;
import com.kratosgado.blog.dtos.request.CreateCommentRequest;
import com.kratosgado.blog.dtos.response.CommentResponse;
import com.kratosgado.blog.enums.CommentStatus;
import com.kratosgado.blog.models.Comment;
import com.kratosgado.blog.models.User;

@Service
public class CommentService {

  private final CommentRepository commentRepository;
  private final UserService userService;

  public CommentService(CommentRepository commentRepository, UserService userService) {
    this.commentRepository = commentRepository;
    this.userService = userService;
  }

  @Transactional
  public CommentResponse createComment(CreateCommentRequest request, Long userId) {
    User user = userService.getUserById(userId);
    
    Comment comment = new Comment(request.postId(), userId, request.content());
    comment.setStatus(CommentStatus.pending);
    // Populate author snapshot
    comment.setAuthorName(user.getUsername());
    comment.setAuthorAvatarUrl(user.getAvatarUrl());
    comment = commentRepository.save(comment);

    return DtoMapper.toCommentResponse(comment);
  }

  @Transactional
  public CommentResponse approveComment(String commentId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> BlogException.notFound("Comment not found"));

    comment.setStatus(CommentStatus.approved);
    comment = commentRepository.save(comment);

    return DtoMapper.toCommentResponse(comment);
  }

  @Transactional
  public CommentResponse rejectComment(String commentId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> BlogException.notFound("Comment not found"));

    comment.setStatus(CommentStatus.rejected);
    comment = commentRepository.save(comment);

    return DtoMapper.toCommentResponse(comment);
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

  }

  public Page<CommentResponse> getPostComments(Long postId, Pageable pageable) {
    Page<Comment> comments = commentRepository.findByPostIdAndStatus(postId, CommentStatus.approved, pageable);
    return comments.map(DtoMapper::toCommentResponse);
  }

  public Page<CommentResponse> getAllPostComments(Long postId, Pageable pageable) {
    Page<Comment> comments = commentRepository.findByPostId(postId, pageable);
    return comments.map(DtoMapper::toCommentResponse);
  }

  public Page<CommentResponse> getUserComments(Long userId, Pageable pageable) {
    Page<Comment> comments = commentRepository.findByUserId(userId, pageable);
    return comments.map(DtoMapper::toCommentResponse);
  }

  public Long getPostCommentCount(Long postId) {
    return commentRepository.countByPostIdAndStatus(postId, CommentStatus.approved);
  }
}
