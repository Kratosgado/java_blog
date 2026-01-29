package com.kratosgado.blog.backend.repositories.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.models.Category;

@Repository
public class CategoryRepository extends SluggableRepository<Category> {

  public CategoryRepository(DataSource dataSource) {
    super(dataSource, Category.class);
    tableName = "categories";
  }

  @Override
  protected void initTable() {
    String sql = """
        CREATE TABLE IF NOT EXISTS categories (
            id BIGSERIAL PRIMARY KEY,
            name VARCHAR(100) UNIQUE NOT NULL,
            slug VARCHAR(100) UNIQUE NOT NULL,
            description TEXT
        );
        CREATE INDEX IF NOT EXISTS idx_categories_slug ON categories(slug);
        """;
    safeExecuteQuery(sql, null);
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

  public Optional<Category> findByName(String name) {
    String query = "SELECT * FROM categories WHERE name = ?";
    return withConnection(conn -> {
      try (PreparedStatement statement = conn.prepareStatement(query)) {
        statement.setString(1, name);
        try (ResultSet rs = statement.executeQuery()) {
          if (rs.next()) {
            return Optional.of(toEntity(rs));
          }
        }
      } catch (SQLException e) {
        throw BlogException.internal("Failed to find category by name: " + name + ": " + e.getMessage());
      }
      return Optional.empty();
    });
  }

  public boolean existsByName(String name) {
    String query = "SELECT COUNT(*) FROM categories WHERE name = ?";
    return withConnection(conn -> {
      try (PreparedStatement statement = conn.prepareStatement(query)) {
        statement.setString(1, name);
        try (ResultSet rs = statement.executeQuery()) {
          if (rs.next()) {
            return rs.getInt(1) > 0;
          }
        }
      } catch (SQLException e) {
        throw BlogException.internal("Failed to count categories by name: " + name + ": " + e.getMessage());
      }
      return false;
    });
  }

}
