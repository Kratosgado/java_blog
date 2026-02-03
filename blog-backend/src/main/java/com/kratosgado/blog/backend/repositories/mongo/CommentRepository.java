package com.kratosgado.blog.backend.repositories.mongo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.kratosgado.blog.dtos.response.CommentResponse.CommentWithoutUser;
import com.kratosgado.blog.enums.CommentStatus;
import com.kratosgado.blog.models.Comment;

@Repository
public interface CommentRepository extends MongoRepository<Comment, String> {
  Page<Comment> findByPostId(Long postId, Pageable pageable);

  Page<Comment> findByPostIdAndStatus(Long postId, CommentStatus status, Pageable pageable);

  Page<CommentWithoutUser> findByUserId(Long userId, Pageable pageable);

  long countByPostIdAndStatus(Long postId, CommentStatus status);

  long countByUserId(Long userId);

  @Query(value = "{}", sort = "{ 'created_at' : -1 }")
  java.util.List<Comment> findTopNByOrderByCreatedAtDesc(int limit);
}
