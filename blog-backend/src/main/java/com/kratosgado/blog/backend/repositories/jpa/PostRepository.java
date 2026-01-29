package com.kratosgado.blog.backend.repositories.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kratosgado.blog.enums.PostStatus;
import com.kratosgado.blog.models.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
  @EntityGraph(value = "post-with-details", type = EntityGraph.EntityGraphType.LOAD)
  Page<Post> findByStatus(PostStatus status, Pageable pageable);
  
  @EntityGraph(value = "post-with-details", type = EntityGraph.EntityGraphType.LOAD)
  Page<Post> findByUserUsername(String username, Pageable pageable);
  
  @EntityGraph(value = "post-with-details", type = EntityGraph.EntityGraphType.LOAD)
  Page<Post> findByCategorySlug(String slug, Pageable pageable);

  @EntityGraph(value = "post-with-details", type = EntityGraph.EntityGraphType.LOAD)
  Page<Post> findByUserId(Long userId, Pageable pageable);

  @EntityGraph(value = "post-with-details", type = EntityGraph.EntityGraphType.LOAD)
  Page<Post> findByCategoryId(Long categoryId, Pageable pageable);
  
  @EntityGraph(value = "post-with-details", type = EntityGraph.EntityGraphType.LOAD)
  @Query("SELECT p FROM Post p JOIN p.tags t WHERE t.slug = :tagSlug")
  Page<Post> findByTagSlug(@Param("tagSlug") String tagSlug, Pageable pageable);
  
  @EntityGraph(value = "post-with-details", type = EntityGraph.EntityGraphType.LOAD)
  Optional<Post> findBySlug(String slug);
  
  @EntityGraph(value = "post-with-details", type = EntityGraph.EntityGraphType.LOAD)
  @Query("SELECT p FROM Post p WHERE p.status = 'published' AND (p.title LIKE %:query% OR p.content LIKE %:query%)")
  Page<Post> searchPublishedPosts(@Param("query") String query, Pageable pageable);

  @Query(value = "SELECT * FROM posts WHERE status = 'published' ORDER BY views DESC LIMIT :limit", nativeQuery = true)
  List<Post> findTopNByOrderByViewsDesc(@Param("limit") int limit);

  @Modifying
  @Query("UPDATE Post p SET p.views = p.views + 1 WHERE p.slug = :slug")
  void incrementViews(@Param("slug") String slug);

  long countByStatus(PostStatus status);

  long countByUserId(Long userId);

  @Query("SELECT SUM(p.views) FROM Post p WHERE p.user.id = :userId")
  long sumViewsByUserId(@Param("userId") Long userId);

  @Query(value = "SELECT * FROM posts ORDER BY created_at DESC LIMIT :limit", nativeQuery = true)
  List<Post> findTopNByOrderByCreatedAtDesc(@Param("limit") int limit);
}
