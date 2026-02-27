package com.kratosgado.blog.backend.services;

import com.kratosgado.blog.backend.models.Comment;
import com.kratosgado.blog.backend.models.User;
import com.kratosgado.blog.dtos.request.CreateCommentRequest;
import com.kratosgado.blog.dtos.request.PageRequest;
import com.kratosgado.blog.dtos.response.CommentResponse.CommentWithoutUser;
import com.kratosgado.blog.dtos.response.PageResponse;

public interface CommentService {

  Comment createComment(CreateCommentRequest request, User user);

  Comment approveComment(String commentId);

  Comment rejectComment(String commentId);

  void deleteComment(String commentId, Long userId);

  Comment getCommentById(String commentId);

  PageResponse<Comment> getPostComments(Long postId, PageRequest pageRequest);

  PageResponse<Comment> getAllPostComments(Long postId, PageRequest pageRequest);

  PageResponse<CommentWithoutUser> getUserComments(Long userId, PageRequest pageRequest);

  Long getPostCommentCount(Long postId);
}
