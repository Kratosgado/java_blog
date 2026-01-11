package com.kratosgado.blog.services;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.dao.CommentDAO;
import com.kratosgado.blog.models.Comment;
import com.kratosgado.blog.utils.enums.CommentStatus;
import com.kratosgado.blog.utils.exceptions.BlogExceptions;

public class CommentService {
  private static final Logger logger = LoggerFactory.getLogger(CommentService.class);
  private final CommentDAO commentDAO;

  public CommentService() {
    this.commentDAO = new CommentDAO();
  }

  public boolean createComment(Comment comment) {
    if (comment.getContent() == null || comment.getContent().isEmpty()) {
      throw BlogExceptions.badRequest("Comment content is required");
    }
    if (comment.getContent().length() > 5000) {
      throw BlogExceptions.badRequest("Comment is too long (max 5000 characters)");
    }
    return commentDAO.createComment(comment);
  }

  public boolean updateComment(Comment comment) {
    Optional<Comment> existing = commentDAO.getCommentById(comment.getId());
    if (existing.isEmpty()) {
      throw BlogExceptions.notFound("Comment not found");
    }
    return commentDAO.updateComment(comment);
  }

  public boolean deleteComment(int id) {
    Optional<Comment> comment = commentDAO.getCommentById(id);
    if (comment.isEmpty()) {
      throw BlogExceptions.notFound("Comment not found");
    }
    return commentDAO.deleteComment(id);
  }

  public Optional<Comment> getCommentById(int id) {
    return commentDAO.getCommentById(id);
  }

  public List<Comment> getCommentsByPostId(int postId) {
    return commentDAO.getCommentsByPostId(postId);
  }

  public List<Comment> getCommentsByUserId(int userId) {
    return commentDAO.getCommentsByUserId(userId);
  }

  public List<Comment> getAllComments() {
    return commentDAO.getAllComments();
  }

  public int getCommentCountForPost(int postId) {
    return commentDAO.getCommentCountForPost(postId);
  }

  public boolean approveComment(int id) {
    Optional<Comment> comment = commentDAO.getCommentById(id);
    if (comment.isEmpty()) {
      throw BlogExceptions.notFound("Comment not found");
    }
    boolean updated = commentDAO.updateCommentStatus(id, CommentStatus.APPROVED);
    if (updated) {
      logger.info("Comment approved: {}", id);
    }
    return updated;
  }

  public boolean rejectComment(int id) {
    Optional<Comment> comment = commentDAO.getCommentById(id);
    if (comment.isEmpty()) {
      throw BlogExceptions.notFound("Comment not found");
    }
    boolean updated = commentDAO.updateCommentStatus(id, CommentStatus.REJECTED);
    if (updated) {
      logger.info("Comment rejected: {}", id);
    }
    return updated;
  }

  public List<Comment> getPendingComments() {
    return commentDAO.getCommentsByStatus(CommentStatus.PENDING);
  }

  public List<Comment> getApprovedComments() {
    return commentDAO.getCommentsByStatus(CommentStatus.APPROVED);
  }

  public List<Comment> getRejectedComments() {
    return commentDAO.getCommentsByStatus(CommentStatus.REJECTED);
  }
}
