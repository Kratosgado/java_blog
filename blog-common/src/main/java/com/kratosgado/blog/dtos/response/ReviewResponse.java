
package com.kratosgado.blog.dtos.response;

import java.time.LocalDateTime;

public class ReviewResponse {

  public record AverageRatingResult(Double avgRating) {
  }

  public record ReviewWithoutUser(Long postId, int rating, String title, String content, LocalDateTime createdAt,
      LocalDateTime updatedAt) {
  }
}
