package com.kratosgado.blog.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.config.DatabaseConfig;
import com.kratosgado.blog.models.Category;
import com.kratosgado.blog.utils.interfaces.DAO;

public class CategoryDAO extends DAO {
  private static final Logger logger = LoggerFactory.getLogger(CategoryDAO.class);

  public CategoryDAO() {
    initDatabase();
  }

  @Override
  protected void initDatabase() {
    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement();) {
      String sql = "CREATE TABLE IF NOT EXISTS categories (" +
          "id SERIAL PRIMARY KEY," +
          "name VARCHAR(100) NOT NULL UNIQUE," +
          "slug VARCHAR(100) NOT NULL UNIQUE," +
          "description TEXT," +
          "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
      stmt.executeUpdate(sql);

      // Create post_categories junction table
      String junctionSql = "CREATE TABLE IF NOT EXISTS post_categories (" +
          "post_id INTEGER NOT NULL," +
          "category_id INTEGER NOT NULL," +
          "PRIMARY KEY (post_id, category_id)," +
          "FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE," +
          "FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE)";
      stmt.executeUpdate(junctionSql);

      logger.debug("Categories table initialized successfully");
    } catch (Exception e) {
      logger.error("Failed to initialize categories table", e);
    }
  }

  public boolean createCategory(Category category) {
    String sql = "INSERT INTO categories (name, slug, description) VALUES (?, ?, ?)";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setString(1, category.getName());
      stmt.setString(2, category.getSlug());
      stmt.setString(3, category.getDescription());
      stmt.executeUpdate();
      logger.info("Category created successfully: {}", category.getName());
      return true;
    } catch (Exception e) {
      logger.error("Failed to create category: {}", category.getName(), e);
      return false;
    }
  }

  public boolean updateCategory(Category category) {
    String sql = "UPDATE categories SET name = ?, slug = ?, description = ? WHERE id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setString(1, category.getName());
      stmt.setString(2, category.getSlug());
      stmt.setString(3, category.getDescription());
      stmt.setInt(4, category.getId());
      stmt.executeUpdate();
      logger.info("Category updated successfully: {}", category.getId());
      return true;
    } catch (Exception e) {
      logger.error("Failed to update category: {}", category.getId(), e);
      return false;
    }
  }

  public boolean deleteCategory(int id) {
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

  public Optional<Category> getCategoryById(int id) {
    String sql = "SELECT * FROM categories WHERE id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, id);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return Optional.of(Category.builder()
            .id(rs.getInt("id"))
            .name(rs.getString("name"))
            .slug(rs.getString("slug"))
            .description(rs.getString("description"))
            .createdAt(rs.getTimestamp("created_at"))
            .build());
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
        return Optional.of(Category.builder()
            .id(rs.getInt("id"))
            .name(rs.getString("name"))
            .slug(rs.getString("slug"))
            .description(rs.getString("description"))
            .createdAt(rs.getTimestamp("created_at"))
            .build());
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
        categories.add(Category.builder()
            .id(rs.getInt("id"))
            .name(rs.getString("name"))
            .slug(rs.getString("slug"))
            .description(rs.getString("description"))
            .createdAt(rs.getTimestamp("created_at"))
            .build());
      }
      logger.info("Retrieved {} categories", categories.size());
    } catch (Exception e) {
      logger.error("Failed to get all categories", e);
    }
    return categories;
  }

  public List<Category> getCategoriesByPostId(int postId) {
    List<Category> categories = new ArrayList<>();
    String sql = "SELECT c.* FROM categories c " +
        "INNER JOIN post_categories pc ON c.id = pc.category_id " +
        "WHERE pc.post_id = ? ORDER BY c.name ASC";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, postId);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        categories.add(Category.builder()
            .id(rs.getInt("id"))
            .name(rs.getString("name"))
            .slug(rs.getString("slug"))
            .description(rs.getString("description"))
            .createdAt(rs.getTimestamp("created_at"))
            .build());
      }
      logger.info("Retrieved {} categories for post {}", categories.size(), postId);
    } catch (Exception e) {
      logger.error("Failed to get categories for post: {}", postId, e);
    }
    return categories;
  }

  public boolean addCategoryToPost(int postId, int categoryId) {
    String sql = "INSERT INTO post_categories (post_id, category_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, postId);
      stmt.setInt(2, categoryId);
      stmt.executeUpdate();
      logger.info("Category {} added to post {}", categoryId, postId);
      return true;
    } catch (Exception e) {
      logger.error("Failed to add category {} to post {}", categoryId, postId, e);
      return false;
    }
  }

  public boolean removeCategoryFromPost(int postId, int categoryId) {
    String sql = "DELETE FROM post_categories WHERE post_id = ? AND category_id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, postId);
      stmt.setInt(2, categoryId);
      stmt.executeUpdate();
      logger.info("Category {} removed from post {}", categoryId, postId);
      return true;
    } catch (Exception e) {
      logger.error("Failed to remove category {} from post {}", categoryId, postId, e);
      return false;
    }
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
}
