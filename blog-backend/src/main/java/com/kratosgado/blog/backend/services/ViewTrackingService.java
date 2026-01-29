package com.kratosgado.blog.backend.services;

import com.kratosgado.blog.backend.repositories.jpa.PostRepository;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ViewTrackingService {

  private final PostRepository postRepository;

  public ViewTrackingService(PostRepository postRepository) {
    this.postRepository = postRepository;
  }

  @Async
  @Transactional
  public void incrementViews(String slug) {
    postRepository.incrementViews(slug);
  }
}
