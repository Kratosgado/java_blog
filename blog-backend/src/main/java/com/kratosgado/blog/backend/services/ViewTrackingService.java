package com.kratosgado.blog.backend.services;

import com.kratosgado.blog.backend.repositories.jdbc.PostRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class ViewTrackingService {

  private final PostRepository postRepository;

  public ViewTrackingService(PostRepository postRepository) {
    this.postRepository = postRepository;
  }

  @Async
  public void incrementViews(String slug) {
    postRepository.incrementViews(slug);

  }
}
