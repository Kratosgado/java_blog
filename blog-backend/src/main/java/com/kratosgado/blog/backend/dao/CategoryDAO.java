package com.kratosgado.blog.backend.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Repository;

import com.kratosgado.blog.backend.config.database.DatabaseConfig;
import com.kratosgado.blog.models.Category;

import jakarta.annotation.PostConstruct;

@Repository
@DependsOn("databaseConfig")
public class CategoryDAO extends BaseDAO {
  private static final Logger logger = LoggerFactory.getLogger(CategoryDAO.class);
  
  @Autowired
  private DatabaseConfig databaseConfig;

  @PostConstruct
  @Override
  protected void initDatabase() {
    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement();) {
      String sql = "CREATE TABLE IF NOT EXISTS categories (" +
          "id SERIAL PRIMARY KEY," +
          "name VARCHAR(100) NOT NULL UNIQUE," +
          "slug VARCHAR(100) NOT NULL UNIQUE," +
          "description TEXT)";
      stmt.executeUpdate(sql);

      logger.debug("Categories table initialized successfully");
    } catch (Exception e) {
      logger.error("Failed to initialize categories table", e);
    }
  }

  public Optional<Category> createCategory(Category category) {
    String sql = "INSERT INTO categories (name, slug, description) VALUES (?, ?, ?) RETURNING id";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setString(1, category.getName());
      stmt.setString(2, category.getSlug());
      stmt.setString(3, category.getDescription());
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        category.setId(rs.getInt("id"));
        logger.info("Category created successfully: {}", category.getName());
        return Optional.of(category);
      }
      return Optional.empty();
    } catch (Exception e) {
      logger.error("Failed to create category: {}", category.getName(), e);
      return Optional.empty();
    }
  }

  public Optional<Category> updateCategory(Category category) {
    String sql = "UPDATE categories SET name = ?, slug = ?, description = ? WHERE id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setString(1, category.getName());
      stmt.setString(2, category.getSlug());
      stmt.setString(3, category.getDescription());
      stmt.setInt(4, category.getId());
      int updated = stmt.executeUpdate();
      if (updated > 0) {
        logger.info("Category updated successfully: {}", category.getId());
        return Optional.of(category);
      }
      return Optional.empty();
    } catch (Exception e) {
      logger.error("Failed to update category: {}", category.getId(), e);
      return Optional.empty();
    }
  }

  public boolean deleteCategory(Integer id) {
    String sql = "DELETE FROM categories WHERE id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, id);
      stmt.executeUpdate();
      logger.info("Category deleted successfully: {}", id);
      return true;
    } catch (Exception e) {
      logger.error("Failed to delete category: {}", id, e);
      return false;
    }
  }

  public Optional<Category> getCategoryById(Integer id) {
    String sql = "SELECT * FROM categories WHERE id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, id);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return Optional.of(mapResultSetToCategory(rs));
      }
      return Optional.empty();
    } catch (Exception e) {
      logger.error("Failed to get category by id: {}", id, e);
      return Optional.empty();
    }
  }

  public Optional<Category> getCategoryBySlug(String slug) {
    String sql = "SELECT * FROM categories WHERE slug = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setString(1, slug);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return Optional.of(mapResultSetToCategory(rs));
      }
      return Optional.empty();
    } catch (Exception e) {
      logger.error("Failed to get category by slug: {}", slug, e);
      return Optional.empty();
    }
  }

  public List<Category> getAllCategories() {
    List<Category> categories = new ArrayList<>();
    String sql = "SELECT * FROM categories ORDER BY name ASC";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        categories.add(mapResultSetToCategory(rs));
      }
      logger.info("Retrieved {} categories", categories.size());
    } catch (Exception e) {
      logger.error("Failed to get all categories", e);
    }
    return categories;
  }

  public int getCategoryCount() {
    String sql = "SELECT COUNT(*) FROM categories";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return rs.getInt(1);
      }
    } catch (Exception e) {
      logger.error("Failed to get category count", e);
    }
    return 0;
  }

  private Category mapResultSetToCategory(ResultSet rs) throws Exception {
    Category category = new Category();
    category.setId(rs.getInt("id"));
    category.setName(rs.getString("name"));
    category.setSlug(rs.getString("slug"));
    category.setDescription(rs.getString("description"));
    return category;
  }
}
