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
public class CategoryRepository extends SluggableRepository<Category> {

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

}
