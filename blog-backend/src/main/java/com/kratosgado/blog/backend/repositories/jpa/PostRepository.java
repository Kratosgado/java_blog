package com.kratosgado.blog.backend.repositories.jpa;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kratosgado.blog.models.Post;

import jakarta.transaction.Transactional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
  @EntityGraph(value = "Post.withDetails", type = EntityGraph.EntityGraphType.LOAD)
  Page<Post> findByStatus(String status, Pageable pageable);

  @EntityGraph(value = "Post.withDetails", type = EntityGraph.EntityGraphType.LOAD)
  Page<Post> findByUserId(Long userId, Pageable pageable);

  @EntityGraph(value = "Post.withDetails", type = EntityGraph.EntityGraphType.LOAD)
  Page<Post> findByCategoryId(Long categoryId, Pageable pageable);

  @EntityGraph(value = "Post.withDetails", type = EntityGraph.EntityGraphType.LOAD)
  Page<Post> findByStatusAndUserId(String status, Long userId, Pageable pageable);

  @EntityGraph(value = "Post.withDetails", type = EntityGraph.EntityGraphType.LOAD)
  Optional<Post> findBySlug(String slug);

  @EntityGraph(value = "Post.withDetails", type = EntityGraph.EntityGraphType.LOAD)
  @Query("SELECT p FROM Post p WHERE p.status = 'published' ORDER BY p.createdAt DESC")
  Page<Post> findPublishedPosts(Pageable pageable);

  @EntityGraph(value = "Post.withDetails", type = EntityGraph.EntityGraphType.LOAD)
  @Query("SELECT p FROM Post p WHERE p.status = 'published' AND " +
      "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
      "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
  Page<Post> searchPublishedPosts(@Param("keyword") String keyword, Pageable pageable);

  @EntityGraph(value = "Post.withDetails", type = EntityGraph.EntityGraphType.LOAD)
  Optional<Post> findByIdAndUserId(Long id, Long userId);

  @Override
  @EntityGraph(value = "Post.withDetails", type = EntityGraph.EntityGraphType.LOAD)
  Optional<Post> findById(Long id);

  @Modifying
  @Transactional
  @Query("UPDATE Post p SET p.views = p.views + 1 WHERE p.slug = :slug")
  void incrementViews(@Param("slug") String slug);

  @Query("SELECT COUNT(p) FROM Post p WHERE p.status = 'published'")
  long countPublishedPosts();

  @EntityGraph(value = "Post.withDetails", type = EntityGraph.EntityGraphType.LOAD)
  Post save(Post post);
}
