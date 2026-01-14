package com.kratosgado.blog.backend.repositories.jpa;

import com.kratosgado.blog.models.Comment;
import com.kratosgado.blog.models.CommentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
  Page<Comment> findByPostId(Long postId, Pageable pageable);
  
  Page<Comment> findByPostIdAndStatus(Long postId, CommentStatus status, Pageable pageable);
  
  Page<Comment> findByUserId(Long userId, Pageable pageable);
  
  Long countByPostIdAndStatus(Long postId, CommentStatus status);
}
