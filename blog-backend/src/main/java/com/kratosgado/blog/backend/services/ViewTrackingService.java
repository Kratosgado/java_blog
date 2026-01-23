package com.kratosgado.blog.backend.services;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.kratosgado.blog.backend.repositories.jpa.PostRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ViewTrackingService {

  private final PostRepository postRepository;

  @Async
  @Transactional
  public void incrementViews(String slug) {
    log.info("Incrementing views for post: {}", slug);
    postRepository.incrementViews(slug);
  }
}
