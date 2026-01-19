package com.kratosgado.blog.backend.repositories.jpa;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.kratosgado.blog.models.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
  Page<Post> findByStatus(String status, Pageable pageable);

  Page<Post> findByUserId(Long userId, Pageable pageable);

  Page<Post> findByCategoryId(Long categoryId, Pageable pageable);

  Page<Post> findByStatusAndUserId(String status, Long userId, Pageable pageable);

  @Query("SELECT p FROM Post p WHERE p.status = 'published' ORDER BY p.createdAt DESC")
  Page<Post> findPublishedPosts(Pageable pageable);

  @Query("SELECT p FROM Post p WHERE p.status = 'published' AND " +
      "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
      "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
  Page<Post> searchPublishedPosts(String keyword, Pageable pageable);

  Optional<Post> findByIdAndUserId(Long id, Long userId);
}
