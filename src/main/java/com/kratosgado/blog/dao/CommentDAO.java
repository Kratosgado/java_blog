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
import com.kratosgado.blog.models.Comment;

public class CommentDAO {
  private static final Logger logger = LoggerFactory.getLogger(CommentDAO.class);

  public CommentDAO() {
    initDatabase();
  }

  private void initDatabase() {
    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement();) {
      String sql = "CREATE TABLE IF NOT EXISTS comments (" +
          "id SERIAL PRIMARY KEY," +
          "post_id INTEGER NOT NULL," +
          "user_id INTEGER NOT NULL," +
          "content TEXT NOT NULL," +
          "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
          "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
          "FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE," +
          "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)";
      stmt.executeUpdate(sql);
      logger.debug("Comments table initialized successfully");
    } catch (Exception e) {
      logger.error("Failed to initialize comments table", e);
    }
  }

  public boolean createComment(Comment comment) {
    String sql = "INSERT INTO comments (post_id, user_id, content) VALUES (?, ?, ?)";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, comment.getPostId());
      stmt.setInt(2, comment.getUserId());
      stmt.setString(3, comment.getContent());
      stmt.executeUpdate();
      logger.info("Comment created successfully for post: {}", comment.getPostId());
      return true;
    } catch (Exception e) {
      logger.error("Failed to create comment for post: {}", comment.getPostId(), e);
      return false;
    }
  }

  public boolean updateComment(Comment comment) {
    String sql = "UPDATE comments SET content = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setString(1, comment.getContent());
      stmt.setInt(2, comment.getId());
      stmt.executeUpdate();
      logger.info("Comment updated successfully: {}", comment.getId());
      return true;
    } catch (Exception e) {
      logger.error("Failed to update comment: {}", comment.getId(), e);
      return false;
    }
  }

  public boolean deleteComment(int id) {
    String sql = "DELETE FROM comments WHERE id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, id);
      stmt.executeUpdate();
      logger.info("Comment deleted successfully: {}", id);
      return true;
    } catch (Exception e) {
      logger.error("Failed to delete comment: {}", id, e);
      return false;
    }
  }

  public Optional<Comment> getCommentById(int id) {
    String sql = "SELECT c.*, u.username as author_name FROM comments c " +
        "JOIN users u ON c.user_id = u.id WHERE c.id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, id);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return Optional.of(mapResultSetToComment(rs));
      }
      return Optional.empty();
    } catch (Exception e) {
      logger.error("Failed to fetch comment by id: {}", id, e);
      return Optional.empty();
    }
  }

  public List<Comment> getCommentsByPostId(int postId) {
    String sql = "SELECT c.*, u.username as author_name FROM comments c " +
        "JOIN users u ON c.user_id = u.id WHERE c.post_id = ? ORDER BY c.created_at DESC";
    List<Comment> comments = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, postId);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        comments.add(mapResultSetToComment(rs));
      }
      logger.info("Fetched {} comments for post: {}", comments.size(), postId);
    } catch (Exception e) {
      logger.error("Failed to fetch comments for post: {}", postId, e);
    }
    return comments;
  }

  public List<Comment> getCommentsByUserId(int userId) {
    String sql = "SELECT c.*, u.username as author_name FROM comments c " +
        "JOIN users u ON c.user_id = u.id WHERE c.user_id = ? ORDER BY c.created_at DESC";
    List<Comment> comments = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, userId);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        comments.add(mapResultSetToComment(rs));
      }
      logger.info("Fetched {} comments from user: {}", comments.size(), userId);
    } catch (Exception e) {
      logger.error("Failed to fetch comments for user: {}", userId, e);
    }
    return comments;
  }

  public List<Comment> getAllComments() {
    String sql = "SELECT c.*, u.username as author_name FROM comments c " +
        "JOIN users u ON c.user_id = u.id ORDER BY c.created_at DESC";
    List<Comment> comments = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement();) {
      ResultSet rs = stmt.executeQuery(sql);
      while (rs.next()) {
        comments.add(mapResultSetToComment(rs));
      }
      logger.info("Fetched {} comments", comments.size());
    } catch (Exception e) {
      logger.error("Failed to fetch all comments", e);
    }
    return comments;
  }

  public int getCommentCountForPost(int postId) {
    String sql = "SELECT COUNT(*) as count FROM comments WHERE post_id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, postId);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return rs.getInt("count");
      }
    } catch (Exception e) {
      logger.error("Failed to get comment count for post: {}", postId, e);
    }
    return 0;
  }

  private Comment mapResultSetToComment(ResultSet rs) throws Exception {
    Comment comment = new Comment(
        rs.getInt("post_id"),
        rs.getInt("user_id"),
        rs.getString("content")
    );
    comment.setId(rs.getInt("id"));
    comment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
    comment.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
    comment.setAuthorName(rs.getString("author_name"));
    return comment;
  }
}
