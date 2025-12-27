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
import com.kratosgado.blog.models.Tag;

public class TagDAO {
  private static final Logger logger = LoggerFactory.getLogger(TagDAO.class);

  public TagDAO() {
    initDatabase();
  }

  private void initDatabase() {
    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement();) {
      String sql = "CREATE TABLE IF NOT EXISTS tags (" +
          "id SERIAL PRIMARY KEY," +
          "name VARCHAR(100) NOT NULL UNIQUE," +
          "slug VARCHAR(100) NOT NULL UNIQUE," +
          "description TEXT," +
          "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
      stmt.executeUpdate(sql);
      
      // Create post_tags junction table
      String junctionSql = "CREATE TABLE IF NOT EXISTS post_tags (" +
          "post_id INTEGER NOT NULL," +
          "tag_id INTEGER NOT NULL," +
          "PRIMARY KEY (post_id, tag_id)," +
          "FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE," +
          "FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE)";
      stmt.executeUpdate(junctionSql);
      
      logger.debug("Tags table initialized successfully");
    } catch (Exception e) {
      logger.error("Failed to initialize tags table", e);
    }
  }

  public boolean createTag(Tag tag) {
    String sql = "INSERT INTO tags (name, slug, description) VALUES (?, ?, ?)";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setString(1, tag.getName());
      stmt.setString(2, tag.getSlug());
      stmt.setString(3, tag.getDescription());
      stmt.executeUpdate();
      logger.info("Tag created successfully: {}", tag.getName());
      return true;
    } catch (Exception e) {
      logger.error("Failed to create tag: {}", tag.getName(), e);
      return false;
    }
  }

  public boolean updateTag(Tag tag) {
    String sql = "UPDATE tags SET name = ?, slug = ?, description = ? WHERE id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setString(1, tag.getName());
      stmt.setString(2, tag.getSlug());
      stmt.setString(3, tag.getDescription());
      stmt.setInt(4, tag.getId());
      stmt.executeUpdate();
      logger.info("Tag updated successfully: {}", tag.getId());
      return true;
    } catch (Exception e) {
      logger.error("Failed to update tag: {}", tag.getId(), e);
      return false;
    }
  }

  public boolean deleteTag(int id) {
    String sql = "DELETE FROM tags WHERE id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, id);
      stmt.executeUpdate();
      logger.info("Tag deleted successfully: {}", id);
      return true;
    } catch (Exception e) {
      logger.error("Failed to delete tag: {}", id, e);
      return false;
    }
  }

  public Optional<Tag> getTagById(int id) {
    String sql = "SELECT t.*, COUNT(pt.post_id) as post_count FROM tags t " +
        "LEFT JOIN post_tags pt ON t.id = pt.tag_id WHERE t.id = ? GROUP BY t.id";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, id);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return Optional.of(mapResultSetToTag(rs));
      }
      return Optional.empty();
    } catch (Exception e) {
      logger.error("Failed to fetch tag by id: {}", id, e);
      return Optional.empty();
    }
  }

  public Optional<Tag> getTagBySlug(String slug) {
    String sql = "SELECT t.*, COUNT(pt.post_id) as post_count FROM tags t " +
        "LEFT JOIN post_tags pt ON t.id = pt.tag_id WHERE t.slug = ? GROUP BY t.id";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setString(1, slug);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return Optional.of(mapResultSetToTag(rs));
      }
      return Optional.empty();
    } catch (Exception e) {
      logger.error("Failed to fetch tag by slug: {}", slug, e);
      return Optional.empty();
    }
  }

  public List<Tag> getAllTags() {
    String sql = "SELECT t.*, COUNT(pt.post_id) as post_count FROM tags t " +
        "LEFT JOIN post_tags pt ON t.id = pt.tag_id GROUP BY t.id ORDER BY t.name";
    List<Tag> tags = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement();) {
      ResultSet rs = stmt.executeQuery(sql);
      while (rs.next()) {
        tags.add(mapResultSetToTag(rs));
      }
      logger.info("Fetched {} tags", tags.size());
    } catch (Exception e) {
      logger.error("Failed to fetch all tags", e);
    }
    return tags;
  }

  public List<Tag> getTagsByPostId(int postId) {
    String sql = "SELECT t.*, COUNT(pt.post_id) as post_count FROM tags t " +
        "JOIN post_tags pt ON t.id = pt.tag_id WHERE pt.post_id = ? GROUP BY t.id";
    List<Tag> tags = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, postId);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        tags.add(mapResultSetToTag(rs));
      }
      logger.info("Fetched {} tags for post: {}", tags.size(), postId);
    } catch (Exception e) {
      logger.error("Failed to fetch tags for post: {}", postId, e);
    }
    return tags;
  }

  public boolean addTagToPost(int postId, int tagId) {
    String sql = "INSERT INTO post_tags (post_id, tag_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, postId);
      stmt.setInt(2, tagId);
      stmt.executeUpdate();
      logger.debug("Tag {} added to post {}", tagId, postId);
      return true;
    } catch (Exception e) {
      logger.error("Failed to add tag to post", e);
      return false;
    }
  }

  public boolean removeTagFromPost(int postId, int tagId) {
    String sql = "DELETE FROM post_tags WHERE post_id = ? AND tag_id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, postId);
      stmt.setInt(2, tagId);
      stmt.executeUpdate();
      logger.debug("Tag {} removed from post {}", tagId, postId);
      return true;
    } catch (Exception e) {
      logger.error("Failed to remove tag from post", e);
      return false;
    }
  }

  private Tag mapResultSetToTag(ResultSet rs) throws Exception {
    return new Tag(
        rs.getInt("id"),
        rs.getString("name"),
        rs.getString("slug"),
        rs.getString("description"),
        rs.getTimestamp("created_at").toLocalDateTime(),
        rs.getInt("post_count")
    );
  }
}
