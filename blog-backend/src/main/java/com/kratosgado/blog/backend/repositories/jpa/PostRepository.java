package com.kratosgado.blog.backend.repositories.jpa;

import com.kratosgado.blog.backend.models.Post;
import com.kratosgado.blog.dtos.response.PostResponse.PostDetails;
import com.kratosgado.blog.dtos.response.PostResponse.PostView;
import com.kratosgado.blog.dtos.response.PostResponse.PostWithoutCategory;
import com.kratosgado.blog.dtos.response.PostResponse.PostWithoutTag;
import com.kratosgado.blog.dtos.response.PostResponse.PostWithoutUser;
import com.kratosgado.blog.enums.PostStatus;
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

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
  @EntityGraph(value = "post-with-details", type = EntityGraph.EntityGraphType.LOAD)
  Page<PostView> findByStatus(PostStatus status, Pageable pageable);

  @EntityGraph(value = "post-without-user", type = EntityGraph.EntityGraphType.LOAD)
  Page<PostWithoutUser> findByUserUsername(String username, Pageable pageable);

  @EntityGraph(value = "post-without-category", type = EntityGraph.EntityGraphType.LOAD)
  Page<PostWithoutCategory> findByCategorySlug(String slug, Pageable pageable);

  @EntityGraph(value = "post-without-user", type = EntityGraph.EntityGraphType.LOAD)
  Page<PostWithoutUser> findByUserId(Long userId, Pageable pageable);

  @EntityGraph(value = "post-without-category", type = EntityGraph.EntityGraphType.LOAD)
  Page<PostWithoutCategory> findByCategoryId(Long categoryId, Pageable pageable);

  @EntityGraph(value = "post-with-details", type = EntityGraph.EntityGraphType.LOAD)
  @Query("SELECT p FROM Post p WHERE p.id = :id")
  Optional<PostDetails> findPostDetailsById(@Param("id") Long id);

  @EntityGraph(value = "post-without-tags", type = EntityGraph.EntityGraphType.LOAD)
  @Query("SELECT p FROM Post p JOIN p.tags t WHERE t.slug = :tagSlug")
  Page<PostWithoutTag> findByTagSlug(@Param("tagSlug") String tagSlug, Pageable pageable);

  @EntityGraph(value = "post-with-details", type = EntityGraph.EntityGraphType.LOAD)
  Optional<PostDetails> findBySlug(String slug);

  @Query(
      value =
          "SELECT * FROM posts WHERE status = 'published' AND search_vector @@"
              + " websearch_to_tsquery('english', :searchTerm)",
      countQuery =
          "SELECT count(*) FROM posts WHERE status = 'published' AND search_vector @@"
              + " websearch_to_tsquery('english', :searchTerm)",
      nativeQuery = true)
  Page<PostView> searchPublishedPosts(@Param("searchTerm") String searchTerm, Pageable pageable);

  // JPQL query - Entity graph applicable for eager loading
  @EntityGraph(value = "post-with-details", type = EntityGraph.EntityGraphType.LOAD)
  @Query(
      "SELECT p FROM Post p WHERE p.status = 'published' AND "
          + "(LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR "
          + "LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%')))")
  Page<PostView> searchPublishedPostsSimple(@Param("query") String query, Pageable pageable);

  @Query("SELECT p FROM Post p WHERE p.status = 'published' ORDER BY p.views DESC LIMIT :limit")
  List<PostView> findTopNByOrderByViewsDesc(@Param("limit") int limit);

  @Modifying
  @Query("UPDATE Post p SET p.views = p.views + 1 WHERE p.slug = :slug")
  void incrementViews(@Param("slug") String slug);

  @Modifying
  @Query("UPDATE Post p SET p.views = p.views + :count WHERE p.slug = :slug")
  void incrementViewsBy(@Param("slug") String slug, @Param("count") int count);

  long countByStatus(PostStatus status);

  long countByUserId(Long userId);

  @Query("SELECT COALESCE(SUM(p.views), 0) FROM Post p WHERE p.user.id = :userId")
  long sumViewsByUserId(@Param("userId") Long userId);

  @Query(value = "SELECT * FROM posts ORDER BY created_at DESC LIMIT :limit", nativeQuery = true)
  List<PostView> findTopNByOrderByCreatedAtDesc(@Param("limit") int limit);

  @Query(
      value =
          "SELECT p from Post p where p.status = :status and p.createdAt >= :sinceDate order by"
              + " p.views desc, p.createdAt desc")
  @EntityGraph(value = "post-with-details", type = EntityGraph.EntityGraphType.LOAD)
  Page<PostView> findTrendingPosts(
      @Param("status") PostStatus status,
      @Param("sinceDate") java.time.LocalDateTime sinceDate,
      Pageable pageable);

  @Query(
      value =
          "SELECT p.* FROM posts p "
              + "WHERE p.category_id = :categoryId AND p.status = 'published' "
              + "ORDER BY p.created_at DESC",
      countQuery =
          "SELECT COUNT(*) FROM posts WHERE category_id = :categoryId AND status = 'published'",
      nativeQuery = true)
  Page<PostView> findPublishedPostsByCategoryOptimized(
      @Param("categoryId") Long categoryId, Pageable pageable);

  @Query(
      value =
          "SELECT DISTINCT p.* FROM posts p "
              + "INNER JOIN post_tags pt ON p.id = pt.post_id "
              + "WHERE pt.tag_id = :tagId AND p.status = 'published' "
              + "ORDER BY p.created_at DESC",
      countQuery =
          "SELECT COUNT(DISTINCT p.id) FROM posts p "
              + "INNER JOIN post_tags pt ON p.id = pt.post_id "
              + "WHERE pt.tag_id = :tagId AND p.status = 'published'",
      nativeQuery = true)
  Page<PostView> findPublishedPostsByTagOptimized(@Param("tagId") Long tagId, Pageable pageable);

  @Query(
      "SELECT COUNT(p) FROM Post p WHERE p.user.id = :userId AND p.status = :status AND p.createdAt"
          + " >= :since")
  long countUserPostsSince(
      @Param("userId") Long userId,
      @Param("status") PostStatus status,
      @Param("since") java.time.LocalDateTime since);

  @Query("SELECT p.status, COUNT(p) FROM Post p WHERE p.user.id = :userId GROUP BY p.status")
  List<Object[]> countPostsByStatusForUser(@Param("userId") Long userId);
}
