package com.kratosgado.blog.backend.repositories.mongo;

import com.kratosgado.blog.models.Comment;
import com.kratosgado.blog.models.CommentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends MongoRepository<Comment, String> {
  Page<Comment> findByPostId(Long postId, Pageable pageable);
  
  Page<Comment> findByPostIdAndStatus(Long postId, CommentStatus status, Pageable pageable);
  
  Page<Comment> findByUserId(Long userId, Pageable pageable);
  
  Long countByPostIdAndStatus(Long postId, CommentStatus status);
}
