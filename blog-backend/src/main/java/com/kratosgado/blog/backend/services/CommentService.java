package com.kratosgado.blog.backend.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.jpa.PostRepository;
import com.kratosgado.blog.backend.repositories.mongo.CommentRepository;
import com.kratosgado.blog.backend.utils.DtoMapper;
import com.kratosgado.blog.dtos.request.CreateCommentRequest;
import com.kratosgado.blog.dtos.request.PageRequest;
import com.kratosgado.blog.dtos.response.CommentResponse.CommentWithoutUser;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.enums.CommentStatus;
import com.kratosgado.blog.models.Comment;
import com.kratosgado.blog.models.User;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@AllArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
public class CommentService {

  private final CommentRepository commentRepository;
  private final PostRepository postRepository;

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public Comment createComment(CreateCommentRequest request, User user) {
    if (!postRepository.existsById(request.postId())) {
      throw BlogException.notFound("Post", "id", request.postId());
    }

    Comment comment = Comment.builder()
        .postId(request.postId())
        .userId(user.getId())
        .content(request.content())
        .status(CommentStatus.pending)
        .authorName(user.getUsername())
        .authorAvatarUrl(user.getAvatarUrl())
        .build();
    comment.onCreate();

    Comment saved = commentRepository.save(comment);

    log.debug("Created comment with ID: {}", saved.getId());
    return saved;
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public Comment approveComment(String commentId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> BlogException.notFound("Comment", "id", commentId));

    comment.setStatus(CommentStatus.approved);
    comment.onUpdate();
    comment = commentRepository.save(comment);

    log.debug("Approved comment with ID: {}", commentId);
    return comment;
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public Comment rejectComment(String commentId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> BlogException.notFound("Comment", "id", commentId));

    comment.setStatus(CommentStatus.rejected);
    comment.onUpdate();
    comment = commentRepository.save(comment);

    log.debug("Rejected comment with ID: {}", commentId);
    return comment;
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public void deleteComment(String commentId, Long userId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> BlogException.notFound("Comment", "id", commentId));

    if (!comment.getUserId().equals(userId)) {
      throw BlogException.unauthorized("You are not allowed to delete this comment");
    }

    commentRepository.delete(comment);

    log.debug("Deleted comment with ID: {}", commentId);
  }

  public Comment getCommentById(String commentId) {
    return commentRepository.findById(commentId)
        .orElseThrow(() -> BlogException.notFound("Comment", "id", commentId));
  }

  public PageResponse<Comment> getPostComments(Long postId, PageRequest pageRequest) {
    Pageable pageable = pageRequest.toPageable();
    Page<Comment> commentPage = commentRepository.findByPostIdAndStatus(postId, CommentStatus.approved, pageable);

    return DtoMapper.toPageResponse(commentPage);
  }

  public PageResponse<Comment> getAllPostComments(Long postId,
      PageRequest pageRequest) {
    Pageable pageable = pageRequest.toPageable();
    Page<Comment> commentPage = commentRepository.findByPostId(postId, pageable);

    return DtoMapper.toPageResponse(commentPage);
  }

  public PageResponse<CommentWithoutUser> getUserComments(Long userId,
      PageRequest pageRequest) {
    Pageable pageable = pageRequest.toPageable();
    Page<CommentWithoutUser> commentPage = commentRepository.findByUserId(userId, pageable);

    return DtoMapper.toPageResponse(commentPage);
  }

  public Long getPostCommentCount(Long postId) {
    return commentRepository.countByPostIdAndStatus(postId, CommentStatus.approved);
  }
}
