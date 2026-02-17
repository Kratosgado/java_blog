package com.kratosgado.blog.backend.repositories.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kratosgado.blog.dtos.response.TagResponse;
import com.kratosgado.blog.backend.models.Tag;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
  Optional<Tag> findByName(String name);
  Optional<Tag> findBySlug(String slug);

  @Query("SELECT new com.kratosgado.blog.dtos.response.TagResponse(t.id, t.name, t.slug, t.description, COUNT(p)) " +
         "FROM Tag t LEFT JOIN t.posts p " +
         "GROUP BY t.id, t.name, t.slug, t.description")
  List<TagResponse> findAllWithPostCount();

  @Query("SELECT t FROM Tag t WHERE t.name LIKE %:keyword% OR t.description LIKE %:keyword%")
  Page<Tag> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
