package com.kratosgado.blog.backend.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.kratosgado.blog.backend.repositories.mongo.CommentRepository;
import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.utils.DtoMapper;
import com.kratosgado.blog.dtos.request.CreateCommentRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.CommentResponse.CommentWithoutUser;
import com.kratosgado.blog.enums.CommentStatus;
import com.kratosgado.blog.models.Comment;
import com.kratosgado.blog.models.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;

  public Comment createComment(CreateCommentRequest request, User user) {

    Comment comment = new Comment(request.postId(), Long.valueOf(user.getId()), request.content());
    comment.setStatus(CommentStatus.pending);
    // Populate author snapshot
    comment.setAuthorName(user.getUsername());
    comment.setAuthorAvatarUrl(user.getAvatarUrl());

    Comment saved = commentRepository.save(comment);

    log.debug("Created comment with ID: {}", saved.getId());
    return saved;
  }

  public Comment approveComment(String commentId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> BlogException.notFound("Comment", "id", commentId));

    comment.setStatus(CommentStatus.approved);
    comment = commentRepository.save(comment);

    log.debug("Approved comment with ID: {}", commentId);
    return comment;
  }

  public Comment rejectComment(String commentId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> BlogException.notFound("Comment", "id", commentId));

    comment.setStatus(CommentStatus.rejected);
    comment = commentRepository.save(comment);

    log.debug("Rejected comment with ID: {}", commentId);
    return comment;
  }

  public void deleteComment(String commentId, Long userId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> BlogException.notFound("Comment", "id", commentId));

    // Only the comment author can delete their comment
    if (!comment.getUserId().equals(userId)) {
      throw BlogException.unauthorized("You are not allowed to delete this comment");
    }

    commentRepository.deleteById(commentId);

    log.debug("Deleted comment with ID: {}", commentId);
  }

  public Comment getCommentById(String commentId) {
    // Try to get from cache first
    log.debug("Cache miss for comment ID: {}, fetching from database", commentId);

    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> BlogException.notFound("Comment", "id", commentId));
    return comment;
  }

  public PageResponse<Comment> getPostComments(Long postId, Pageable pageable) {
    Page<Comment> commentPage = commentRepository.findByPostIdAndStatus(postId, CommentStatus.approved, pageable);
    return DtoMapper.toPageResponse(commentPage, pageable);
  }

  public PageResponse<Comment> getPostComments(Long postId, int page, int size) {
    return getPostComments(postId, PageRequest.of(page - 1, size));
  }

  public PageResponse<Comment> getAllPostComments(Long postId, Pageable pageable) {
    Page<Comment> commentPage = commentRepository.findByPostId(postId, pageable);
    return DtoMapper.toPageResponse(commentPage, pageable);
  }

  public PageResponse<Comment> getAllPostComments(Long postId, int page, int size) {
    return getAllPostComments(postId, PageRequest.of(page - 1, size));
  }

  public PageResponse<CommentWithoutUser> getUserComments(Long userId, Pageable pageable) {
    var commentPage = commentRepository.findByUserId(userId, pageable);
    return DtoMapper.toPageResponse(commentPage, pageable);
  }

  public PageResponse<CommentWithoutUser> getUserComments(Long userId, int page, int size) {
    return getUserComments(userId, PageRequest.of(page - 1, size));
  }

  public Long getPostCommentCount(Long postId) {
    return commentRepository.countByPostIdAndStatus(postId, CommentStatus.approved);
  }
}
