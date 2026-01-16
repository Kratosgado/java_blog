package com.kratosgado.blog.backend.repositories.jpa;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.kratosgado.blog.models.Tag;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
  
  Optional<Tag> findBySlug(String slug);
  
  Optional<Tag> findByName(String name);
  
  boolean existsBySlug(String slug);
  
  boolean existsByName(String name);
  
  @Query("SELECT t FROM Tag t ORDER BY t.createdAt DESC")
  Page<Tag> findAllOrderByCreatedAtDesc(Pageable pageable);
  
  @Query("SELECT t FROM Tag t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
  Page<Tag> searchByName(String keyword, Pageable pageable);
}
