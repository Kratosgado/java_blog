package com.kratosgado.blog.backend.repositories.mongo;

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

  boolean existsByPostIdAndUserId(Long postId, Long userId);

  long countByPostId(Long postId);

  long countByUserId(Long userId);

  @Aggregation(pipeline = {
      "{ '$match': { 'post_id': ?0 } }",
      "{ '$group': { '_id': null, 'avgRating': { '$avg': '$rating' } } }"
  })
  Double getAverageRating(Long postId);
}
