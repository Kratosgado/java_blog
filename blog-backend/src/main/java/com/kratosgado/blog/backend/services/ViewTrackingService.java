package com.kratosgado.blog.backend.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kratosgado.blog.backend.exceptions.InvalidRequestException;
import com.kratosgado.blog.backend.repositories.jpa.PostRepository;
import com.kratosgado.blog.backend.utils.BlogConstants.Miliseconds;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ViewTrackingService {

  private final PostRepository postRepository;
  private final ObjectMapper objectMapper; // Autowired Jackson mapper

  //
  private final ConcurrentHashMap<String, AtomicInteger> viewBuffer = new ConcurrentHashMap<>();

  public void incrementViews(String slug) {
    // Increment the view count in the buffer atomically
    viewBuffer.computeIfAbsent(slug, s -> new AtomicInteger(0)).incrementAndGet();
  }

  // Flush the buffer to the database every 10 seconds
  @Scheduled(fixedDelay = Miliseconds.ONE_MINUTE)
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public void flushViews() {
    if (viewBuffer.isEmpty()) return;
    // Extract and clear the buffer
    List<Map<String, Object>> updateList = new ArrayList<>();
    viewBuffer.forEach(
        (slug, count) -> {
          Integer currentCount = viewBuffer.get(slug).intValue();
          if (currentCount != null) {
            updateList.add(Map.of("slug", slug, "count", currentCount));
          }
        });

    try {
      String json = objectMapper.writeValueAsString(updateList);
      postRepository.incrementViewsBy(json);
    } catch (JsonProcessingException e) {
      // Log error and handle retry/recovery
      throw new InvalidRequestException("Failed to serialize view updates", e);
    }
  }
}
