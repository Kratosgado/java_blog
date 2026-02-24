package com.kratosgado.blog.backend.services;

import com.kratosgado.blog.backend.repositories.jpa.PostRepository;
import com.kratosgado.blog.backend.utils.BlogConstants.Miliseconds;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ViewTrackingService {

  private final PostRepository postRepository;

  // Thread-safe map to buffer view counts
  private final ConcurrentHashMap<String, Integer> viewBuffer = new ConcurrentHashMap<>();

  @Async
  public void incrementViews(String slug) {
    // Increment the view count in the buffer atomically
    viewBuffer.merge(slug, 1, Integer::sum);
  }

  // Flush the buffer to the database every 10 seconds
  @Scheduled(fixedDelay = Miliseconds.ONE_MINUTE)
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public void flushViews() {
    if (viewBuffer.isEmpty()) {
      return;
    }

    // Create a snapshot of the current buffer and clear it
    Map<String, Integer> snapshot = new ConcurrentHashMap<>(viewBuffer);
    viewBuffer.clear();

    // Update the database with the buffered counts
    for (Map.Entry<String, Integer> entry : snapshot.entrySet()) {
      String slug = entry.getKey();
      int count = entry.getValue();

      // We need a new method in PostRepository to increment by a specific amount
      // For now, we can just call the existing incrementViews method 'count' times
      // or better, add a new method to PostRepository.
      postRepository.incrementViewsBy(slug, count);
    }
  }
}
