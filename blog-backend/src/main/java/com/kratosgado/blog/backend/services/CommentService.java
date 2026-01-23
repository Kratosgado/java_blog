package com.kratosgado.blog.backend.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kratosgado.blog.backend.dao.nosql.CommentMongoDAO;
import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.dtos.request.CreateCommentRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.enums.CommentStatus;
import com.kratosgado.blog.models.Comment;
import com.kratosgado.blog.models.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentService {

  private final CommentMongoDAO commentDAO;

  public Comment createComment(CreateCommentRequest request, User user) {

    Comment comment = new Comment(request.postId(), Long.valueOf(user.getId()), request.content());
    comment.setStatus(CommentStatus.pending);
    // Populate author snapshot
    comment.setAuthorName(user.getUsername());
    comment.setAuthorAvatarUrl(user.getAvatarUrl());

    Comment saved = commentDAO.createComment(comment)
        .orElseThrow(() -> BlogException.internal("Failed to create comment"));

    log.debug("Created comment with ID: {}", saved.getId());
    return saved;
  }

  public Comment approveComment(String commentId) {
    Comment comment = commentDAO.getCommentById(commentId)
        .orElseThrow(() -> BlogException.notFound("Comment", "id", commentId));

    comment.setStatus(CommentStatus.approved);

    if (!commentDAO.updateComment(commentId, comment)) {
      throw BlogException.internal("Failed to approve comment");
    }

    log.debug("Approved comment with ID: {}", commentId);
    return comment;
  }

  public Comment rejectComment(String commentId) {
    Comment comment = commentDAO.getCommentById(commentId)
        .orElseThrow(() -> BlogException.notFound("Comment", "id", commentId));

    comment.setStatus(CommentStatus.rejected);

    if (!commentDAO.updateComment(commentId, comment)) {
      throw BlogException.internal("Failed to reject comment");
    }

    log.debug("Rejected comment with ID: {}", commentId);
    return comment;
  }

  public void deleteComment(String commentId, Long userId) {
    Comment comment = commentDAO.getCommentById(commentId)
        .orElseThrow(() -> BlogException.notFound("Comment", "id", commentId));

    // Only the comment author can delete their comment
    if (!comment.getUserId().equals(userId)) {
      throw BlogException.unauthorized("You are not allowed to delete this comment");
    }

    if (!commentDAO.deleteComment(commentId)) {
      throw BlogException.internal("Failed to delete comment");
    }

    log.debug("Deleted comment with ID: {}", commentId);
  }

  public Comment getCommentById(String commentId) {
    // Try to get from cache first
    log.debug("Cache miss for comment ID: {}, fetching from database", commentId);

    Comment comment = commentDAO.getCommentById(commentId)
        .orElseThrow(() -> BlogException.notFound("Comment", "id", commentId));
    return comment;
  }

  public PageResponse<Comment> getPostComments(Long postId, int page, int size) {
    List<Comment> allComments = commentDAO.getCommentsByPostId(postId);

    // Filter for approved comments only
    List<Comment> approvedComments = allComments.stream()
        .filter(comment -> comment.getStatus() == CommentStatus.approved)
        .toList();

    return paginateComments(approvedComments, page, size);
  }

  public PageResponse<Comment> getAllPostComments(Long postId, int page, int size) {
    List<Comment> allComments = commentDAO.getCommentsByPostId(postId);
    return paginateComments(allComments, page, size);
  }

  public PageResponse<Comment> getUserComments(Long userId, int page, int size) {
    List<Comment> userComments = commentDAO.getCommentsByUserId(userId);
    return paginateComments(userComments, page, size);
  }

  public Long getPostCommentCount(Long postId) {
    return commentDAO.getCommentCountForPost(postId);
  }

  private PageResponse<Comment> paginateComments(List<Comment> comments, int page, int size) {
    int totalElements = comments.size();
    int totalPages = (int) Math.ceil((double) totalElements / size);

    int offset = (page - 1) * size;
    int endIndex = Math.min(offset + size, totalElements);

    List<Comment> pagedComments = comments.subList(Math.max(0, offset), Math.max(0, endIndex));

    return new PageResponse<>(
        pagedComments,
        page,
        size,
        totalElements,
        totalPages,
        page < totalPages,
        page > 1);
  }
}
