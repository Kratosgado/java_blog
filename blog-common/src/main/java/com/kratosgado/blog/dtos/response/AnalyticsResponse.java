
package com.kratosgado.blog.dtos.response;

import java.util.List;

public record AnalyticsResponse(
    int totalPosts,
    long totalViews,
    double averageViews,
    List<TopPostData> topPosts) {
  
  public record TopPostData(
      Long id,
      String title,
      long views,
      int likesCount) {
  }
}
