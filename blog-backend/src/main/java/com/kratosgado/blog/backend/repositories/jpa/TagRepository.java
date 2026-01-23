package com.kratosgado.blog.backend.repositories.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kratosgado.blog.models.Tag;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

  Optional<Tag> findBySlug(String slug);

  Optional<Tag> findByName(String name);

  boolean existsBySlug(String slug);

  boolean existsByName(String name);

  @Query("SELECT t FROM Tag t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
  Page<Tag> searchByName(@Param("keyword") String keyword, Pageable pageable);

  @Query("SELECT t FROM Tag t JOIN t.posts p WHERE p.id = :postId")
  List<Tag> findByPostId(@Param("postId") Long postId);
}
