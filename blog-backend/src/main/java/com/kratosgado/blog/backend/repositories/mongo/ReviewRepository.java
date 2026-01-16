package com.kratosgado.blog.backend.repositories.mongo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.kratosgado.blog.models.Review;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {
  
  Page<Review> findByPostId(Long postId, Pageable pageable);
  
  Page<Review> findByUserId(Long userId, Pageable pageable);
  
  Page<Review> findByPostIdOrderByCreatedAtDesc(Long postId, Pageable pageable);
  
  @Aggregation(pipeline = {
    "{ '$match': { 'postId': ?0 } }",
    "{ '$group': { '_id': null, 'avgRating': { '$avg': '$rating' } } }"
  })
  List<AverageRatingResult> getAverageRatingByPostId(Long postId);
  
  Long countByPostId(Long postId);
  
  Optional<Review> findByPostIdAndUserId(Long postId, Long userId);
  
  boolean existsByPostIdAndUserId(Long postId, Long userId);
  
  interface AverageRatingResult {
    Double getAvgRating();
  }
}
