package com.kratosgado.blog.services;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.kratosgado.blog.dao.nosql.CommentMongoDAO;
import com.kratosgado.blog.dtos.request.CreateCommentDto;
import com.kratosgado.blog.models.Comment;
import com.kratosgado.blog.utils.enums.CommentStatus;
import com.kratosgado.blog.utils.validators.ValidatorEngine;

public class CommentService {
  private static final Logger logger = LoggerFactory.getLogger(CommentService.class);
  private final CommentMongoDAO commentMongoDAO;

  @Inject
  public CommentService(CommentMongoDAO commentMongoDAO) {
    this.commentMongoDAO = commentMongoDAO;
    logger.info("CommentService initialized with MongoDB backend");
  }

  public boolean createComment(CreateCommentDto dto) {
    ValidatorEngine.validate(dto);

    Comment comment = new Comment(dto.postId(), dto.userId(), dto.content());
    comment.setAuthorName(dto.authorName());
    comment.setAuthorAvatarUrl(dto.authorAvatarUrl());

    Optional<Comment> result = commentMongoDAO.createComment(comment);
    if (result.isPresent()) {
      logger.info("Comment created successfully for post: {}", dto.postId());
      return true;
    }
    return false;
  }

  public List<Comment> getCommentsByPostId(int postId) {
    return commentMongoDAO.getCommentsByPostId(postId);
  }

  public List<Comment> getCommentsByUserId(int userId) {
    return commentMongoDAO.getCommentsByUserId(userId);
  }

  public List<Comment> getAllComments() {
    return commentMongoDAO.getAllComments();
  }

  public int getCommentCountForPost(int postId) {
    return commentMongoDAO.getCommentCountForPost(postId);
  }

  public int getApprovedCommentCountForPost(int postId) {
    return commentMongoDAO.getApprovedCommentCountForPost(postId);
  }

  public List<Comment> getCommentsByStatus(CommentStatus status) {
    return commentMongoDAO.getCommentsByStatus(status.name());
  }

  public List<Comment> getPendingComments() {
    return commentMongoDAO.getCommentsByStatus(CommentStatus.PENDING.name());
  }

  public List<Comment> getApprovedComments() {
    return commentMongoDAO.getCommentsByStatus(CommentStatus.APPROVED.name());
  }

  public List<Comment> getRejectedComments() {
    return commentMongoDAO.getCommentsByStatus(CommentStatus.REJECTED.name());
  }

  public List<Comment> searchComments(String keyword) {
    return commentMongoDAO.searchComments(keyword);
  }

  // Note: These methods are kept for backwards compatibility with controllers
  // MongoDB uses ObjectId internally, but we expose integer IDs for UI
  // compatibility
  // In a production system, these would be refactored to use MongoDB ObjectId
  // strings

  public boolean approveComment(int commentId) {
    logger.warn("approveComment with integer ID is deprecated - MongoDB uses ObjectId strings");
    // This is a simplified stub - full implementation requires storing ID mapping
    return false;
  }

  public boolean rejectComment(int commentId) {
    logger.warn("rejectComment with integer ID is deprecated - MongoDB uses ObjectId strings");
    // This is a simplified stub - full implementation requires storing ID mapping
    return false;
  }

  public boolean deleteComment(int commentId) {
    logger.warn("deleteComment with integer ID is deprecated - MongoDB uses ObjectId strings");
    // This is a simplified stub - full implementation requires storing ID mapping
    return false;
  }
}
