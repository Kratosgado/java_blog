package com.kratosgado.blog.backend.repositories.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.kratosgado.blog.dtos.response.CategoryResponse;
import com.kratosgado.blog.models.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
  Optional<Category> findByName(String name);

  Optional<Category> findBySlug(String slug);

  @Query("SELECT new com.kratosgado.blog.dtos.response.CategoryResponse(c.id, c.name, c.slug, c.description, COUNT(p)) "
      +
      "FROM Category c LEFT JOIN Post p ON p.category = c " +
      "GROUP BY c.id, c.name, c.slug, c.description")
  List<CategoryResponse> findAllWithPostCount();
}
