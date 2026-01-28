package com.kratosgado.blog.backend.repositories.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.models.Category;

@Repository
public class CategoryRepository extends BaseRepository<Category> {

  public CategoryRepository(Connection connection) {
    super(connection, Category.class);
    tableName = "categories";
  }

  @Override
  public Category toEntityFlat(ResultSet rs) throws SQLException {
    Category category = new Category();
    category.setId(rs.getLong("id"));
    category.setName(rs.getString("name"));
    category.setSlug(rs.getString("slug"));
    category.setDescription(rs.getString("description"));
    return category;
  }

  public Optional<Category> findBySlug(String slug) {
    String query = "SELECT * FROM categories WHERE slug = ?";
    try (PreparedStatement statement = connection.prepareStatement(query)) {
      statement.setString(1, slug);
      try (ResultSet rs = statement.executeQuery()) {
        if (rs.next()) {
          return Optional.of(toEntity(rs));
        }
      }
    } catch (SQLException e) {
      throw BlogException.internal("Failed to find by slug: " + slug + ": " + e.getMessage());
    }
    return Optional.empty();
  }

  public Optional<Category> findByName(String name) throws SQLException {
    String query = "SELECT * FROM categories WHERE name = ?";
    try (PreparedStatement statement = connection.prepareStatement(query)) {
      statement.setString(1, name);
      try (ResultSet rs = statement.executeQuery()) {
        if (rs.next()) {
          return Optional.of(toEntity(rs));
        }
      }
    }
    return Optional.empty();
  }

  public boolean existsByName(String name) throws SQLException {
    String query = "SELECT COUNT(*) FROM categories WHERE name = ?";
    try (PreparedStatement statement = connection.prepareStatement(query)) {
      statement.setString(1, name);
      try (ResultSet rs = statement.executeQuery()) {
        if (rs.next()) {
          return rs.getInt(1) > 0;
        }
      }
    }
    return false;
  }

  @Override
  public Long count() {
    String query = "SELECT COUNT(*) FROM categories";
    try (PreparedStatement statement = connection.prepareStatement(query)) {
      try (ResultSet rs = statement.executeQuery()) {
        if (rs.next()) {
          return rs.getLong(1);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return 0L;
  }
}
