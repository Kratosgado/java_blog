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
import com.kratosgado.blog.models.Post;

public class PostDAO {
  private static final Logger logger = LoggerFactory.getLogger(PostDAO.class);

  public PostDAO() {
    initDatabase();
  }

  private void initDatabase() {
    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement();) {
      String sql = "CREATE TABLE IF NOT EXISTS posts (" +
          "id SERIAL PRIMARY KEY," +
          "user_id INTEGER NOT NULL," +
          "title VARCHAR(255) NOT NULL," +
          "content TEXT NOT NULL," +
          "excerpt VARCHAR(500)," +
          "status VARCHAR(20) DEFAULT 'draft'," +
          "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
          "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
          "views INTEGER DEFAULT 0," +
          "featured_image VARCHAR(500)," +
          "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)";
      stmt.executeUpdate(sql);
      logger.debug("Posts table initialized successfully");
    } catch (Exception e) {
      logger.error("Failed to initialize posts table", e);
    }
  }

  public boolean createPost(Post post) {
    String sql = "INSERT INTO posts (user_id, title, content, excerpt, status, featured_image) VALUES (?, ?, ?, ?, ?, ?)";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, post.getUserId());
      stmt.setString(2, post.getTitle());
      stmt.setString(3, post.getContent());
      stmt.setString(4, post.getExcerpt());
      stmt.setString(5, post.getStatus());
      stmt.setString(6, post.getFeaturedImage());
      stmt.executeUpdate();
      logger.info("Post created successfully: {}", post.getTitle());
      return true;
    } catch (Exception e) {
      logger.error("Failed to create post: {}", post.getTitle(), e);
      return false;
    }
  }

  public boolean updatePost(Post post) {
    String sql = "UPDATE posts SET title = ?, content = ?, excerpt = ?, status = ?, featured_image = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setString(1, post.getTitle());
      stmt.setString(2, post.getContent());
      stmt.setString(3, post.getExcerpt());
      stmt.setString(4, post.getStatus());
      stmt.setString(5, post.getFeaturedImage());
      stmt.setInt(6, post.getId());
      stmt.executeUpdate();
      logger.info("Post updated successfully: {}", post.getId());
      return true;
    } catch (Exception e) {
      logger.error("Failed to update post: {}", post.getId(), e);
      return false;
    }
  }

  public boolean deletePost(int id) {
    String sql = "DELETE FROM posts WHERE id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, id);
      stmt.executeUpdate();
      logger.info("Post deleted successfully: {}", id);
      return true;
    } catch (Exception e) {
      logger.error("Failed to delete post: {}", id, e);
      return false;
    }
  }

  public Optional<Post> getPostById(int id) {
    String sql = "SELECT * FROM posts WHERE id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, id);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return Optional.of(mapResultSetToPost(rs));
      }
      return Optional.empty();
    } catch (Exception e) {
      logger.error("Failed to fetch post by id: {}", id, e);
      return Optional.empty();
    }
  }

  public List<Post> getPostsByUserId(int userId) {
    String sql = "SELECT * FROM posts WHERE user_id = ? ORDER BY created_at DESC";
    List<Post> posts = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, userId);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        posts.add(mapResultSetToPost(rs));
      }
      logger.info("Fetched {} posts for user: {}", posts.size(), userId);
    } catch (Exception e) {
      logger.error("Failed to fetch posts for user: {}", userId, e);
    }
    return posts;
  }

  public List<Post> getPostsByStatus(String status) {
    String sql = "SELECT * FROM posts WHERE status = ? ORDER BY created_at DESC";
    List<Post> posts = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setString(1, status);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        posts.add(mapResultSetToPost(rs));
      }
      logger.info("Fetched {} posts with status: {}", posts.size(), status);
    } catch (Exception e) {
      logger.error("Failed to fetch posts with status: {}", status, e);
    }
    return posts;
  }

  public List<Post> getAllPosts() {
    String sql = "SELECT * FROM posts ORDER BY created_at DESC";
    List<Post> posts = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement();) {
      ResultSet rs = stmt.executeQuery(sql);
      while (rs.next()) {
        posts.add(mapResultSetToPost(rs));
      }
      logger.info("Fetched {} posts", posts.size());
    } catch (Exception e) {
      logger.error("Failed to fetch all posts", e);
    }
    return posts;
  }

  public boolean incrementViews(int postId) {
    String sql = "UPDATE posts SET views = views + 1 WHERE id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, postId);
      stmt.executeUpdate();
      logger.debug("Views incremented for post: {}", postId);
      return true;
    } catch (Exception e) {
      logger.error("Failed to increment views for post: {}", postId, e);
      return false;
    }
  }

  private Post mapResultSetToPost(ResultSet rs) throws Exception {
    return new Post(
        rs.getInt("id"),
        rs.getInt("user_id"),
        rs.getString("title"),
        rs.getString("content"),
        rs.getString("excerpt"),
        rs.getString("status"),
        rs.getTimestamp("created_at").toLocalDateTime(),
        rs.getTimestamp("updated_at").toLocalDateTime(),
        rs.getInt("views"),
        rs.getString("featured_image"));
  }
}
