package com.kratosgado.blog.backend.repositories.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.kratosgado.blog.models.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
  
  Page<Review> findByPostId(Long postId, Pageable pageable);
  
  Page<Review> findByUserId(Long userId, Pageable pageable);
  
  @Query("SELECT r FROM Review r WHERE r.postId = :postId ORDER BY r.createdAt DESC")
  Page<Review> findByPostIdOrderByCreatedAtDesc(Long postId, Pageable pageable);
  
  @Query("SELECT AVG(r.rating) FROM Review r WHERE r.postId = :postId")
  Double getAverageRatingByPostId(Long postId);
  
  @Query("SELECT COUNT(r) FROM Review r WHERE r.postId = :postId")
  Long countByPostId(Long postId);
  
  Optional<Review> findByPostIdAndUserId(Long postId, Long userId);
  
  boolean existsByPostIdAndUserId(Long postId, Long userId);
}
