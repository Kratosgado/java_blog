package com.kratosgado.blog.backend.services;

import com.kratosgado.blog.backend.repositories.jpa.PostRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ViewTrackingService {

  private final PostRepository postRepository;

  @Async
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public void incrementViews(String slug) {
    postRepository.incrementViews(slug);
  }
}
