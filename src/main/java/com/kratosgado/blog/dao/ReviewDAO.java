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
import com.kratosgado.blog.models.Review;
import com.kratosgado.blog.utils.interfaces.DAO;

public class ReviewDAO extends DAO {
  private static final Logger logger = LoggerFactory.getLogger(ReviewDAO.class);

  public ReviewDAO() {
    initDatabase();
  }

  @Override
  protected void initDatabase() {
    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement();) {
      String sql = "CREATE TABLE IF NOT EXISTS reviews (" +
          "id SERIAL PRIMARY KEY," +
          "post_id INTEGER NOT NULL," +
          "user_id INTEGER NOT NULL," +
          "rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5)," +
          "title VARCHAR(255)," +
          "content TEXT," +
          "helpful BOOLEAN DEFAULT FALSE," +
          "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
          "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
          "FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE," +
          "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)";
      stmt.executeUpdate(sql);
      logger.debug("Reviews table initialized successfully");
      
      // Create indexes for performance optimization
      createIndexes(conn);
    } catch (Exception e) {
      logger.error("Failed to initialize reviews table", e);
    }
  }
  
  private void createIndexes(Connection conn) {
    try (Statement stmt = conn.createStatement()) {
      // Index on post_id for quick post review retrieval
      stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_reviews_post_id ON reviews(post_id)");
      // Index on user_id for quick user review retrieval
      stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_reviews_user_id ON reviews(user_id)");
      // Index on rating for filtering by rating
      stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_reviews_rating ON reviews(rating)");
      logger.debug("Review indexes created successfully");
    } catch (Exception e) {
      logger.error("Failed to create review indexes", e);
    }
  }

  public boolean createReview(Review review) {
    String sql = "INSERT INTO reviews (post_id, user_id, rating, title, content, helpful) VALUES (?, ?, ?, ?, ?, ?)";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, review.getPostId());
      stmt.setInt(2, review.getUserId());
      stmt.setInt(3, review.getRating());
      stmt.setString(4, review.getTitle());
      stmt.setString(5, review.getContent());
      stmt.setBoolean(6, review.isHelpful());
      stmt.executeUpdate();
      logger.info("Review created successfully for post: {}", review.getPostId());
      return true;
    } catch (Exception e) {
      logger.error("Failed to create review for post: {}", review.getPostId(), e);
      return false;
    }
  }

  public boolean updateReview(Review review) {
    String sql = "UPDATE reviews SET rating = ?, title = ?, content = ?, helpful = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, review.getRating());
      stmt.setString(2, review.getTitle());
      stmt.setString(3, review.getContent());
      stmt.setBoolean(4, review.isHelpful());
      stmt.setInt(5, review.getId());
      stmt.executeUpdate();
      logger.info("Review updated successfully: {}", review.getId());
      return true;
    } catch (Exception e) {
      logger.error("Failed to update review: {}", review.getId(), e);
      return false;
    }
  }

  public boolean deleteReview(int id) {
    String sql = "DELETE FROM reviews WHERE id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, id);
      stmt.executeUpdate();
      logger.info("Review deleted successfully: {}", id);
      return true;
    } catch (Exception e) {
      logger.error("Failed to delete review: {}", id, e);
      return false;
    }
  }

  public Optional<Review> getReviewById(int id) {
    String sql = "SELECT r.*, u.username as author_name, u.avatar_url as author_avatar_url FROM reviews r " +
        "JOIN users u ON r.user_id = u.id WHERE r.id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, id);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return Optional.of(mapResultSetToReview(rs));
      }
      return Optional.empty();
    } catch (Exception e) {
      logger.error("Failed to fetch review by id: {}", id, e);
      return Optional.empty();
    }
  }

  public List<Review> getReviewsByPostId(int postId) {
    String sql = "SELECT r.*, u.username as author_name, u.avatar_url as author_avatar_url FROM reviews r " +
        "JOIN users u ON r.user_id = u.id WHERE r.post_id = ? ORDER BY r.created_at DESC";
    List<Review> reviews = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, postId);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        reviews.add(mapResultSetToReview(rs));
      }
      logger.info("Fetched {} reviews for post: {}", reviews.size(), postId);
    } catch (Exception e) {
      logger.error("Failed to fetch reviews for post: {}", postId, e);
    }
    return reviews;
  }

  public List<Review> getReviewsByUserId(int userId) {
    String sql = "SELECT r.*, u.username as author_name, u.avatar_url as author_avatar_url FROM reviews r " +
        "JOIN users u ON r.user_id = u.id WHERE r.user_id = ? ORDER BY r.created_at DESC";
    List<Review> reviews = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, userId);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        reviews.add(mapResultSetToReview(rs));
      }
      logger.info("Fetched {} reviews from user: {}", reviews.size(), userId);
    } catch (Exception e) {
      logger.error("Failed to fetch reviews for user: {}", userId, e);
    }
    return reviews;
  }

  public List<Review> getAllReviews() {
    String sql = "SELECT r.*, u.username as author_name, u.avatar_url as author_avatar_url FROM reviews r " +
        "JOIN users u ON r.user_id = u.id ORDER BY r.created_at DESC";
    List<Review> reviews = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement();) {
      ResultSet rs = stmt.executeQuery(sql);
      while (rs.next()) {
        reviews.add(mapResultSetToReview(rs));
      }
      logger.info("Fetched {} reviews", reviews.size());
    } catch (Exception e) {
      logger.error("Failed to fetch all reviews", e);
    }
    return reviews;
  }

  public int getReviewCountForPost(int postId) {
    String sql = "SELECT COUNT(*) as count FROM reviews WHERE post_id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, postId);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return rs.getInt("count");
      }
    } catch (Exception e) {
      logger.error("Failed to get review count for post: {}", postId, e);
    }
    return 0;
  }

  public double getAverageRatingForPost(int postId) {
    String sql = "SELECT AVG(rating) as avg_rating FROM reviews WHERE post_id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, postId);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return rs.getDouble("avg_rating");
      }
    } catch (Exception e) {
      logger.error("Failed to get average rating for post: {}", postId, e);
    }
    return 0.0;
  }

  public List<Review> getReviewsByRating(int rating) {
    String sql = "SELECT r.*, u.username as author_name, u.avatar_url as author_avatar_url FROM reviews r " +
        "JOIN users u ON r.user_id = u.id WHERE r.rating = ? ORDER BY r.created_at DESC";
    List<Review> reviews = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, rating);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        reviews.add(mapResultSetToReview(rs));
      }
      logger.info("Fetched {} reviews with rating: {}", reviews.size(), rating);
    } catch (Exception e) {
      logger.error("Failed to fetch reviews by rating: {}", rating, e);
    }
    return reviews;
  }

  public List<Review> getHelpfulReviews() {
    String sql = "SELECT r.*, u.username as author_name, u.avatar_url as author_avatar_url FROM reviews r " +
        "JOIN users u ON r.user_id = u.id WHERE r.helpful = TRUE ORDER BY r.created_at DESC";
    List<Review> reviews = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement();) {
      ResultSet rs = stmt.executeQuery(sql);
      while (rs.next()) {
        reviews.add(mapResultSetToReview(rs));
      }
      logger.info("Fetched {} helpful reviews", reviews.size());
    } catch (Exception e) {
      logger.error("Failed to fetch helpful reviews", e);
    }
    return reviews;
  }

  private Review mapResultSetToReview(ResultSet rs) throws Exception {
    Review review = new Review();
    review.setId(rs.getInt("id"));
    review.setPostId(rs.getInt("post_id"));
    review.setUserId(rs.getInt("user_id"));
    review.setRating(rs.getInt("rating"));
    review.setTitle(rs.getString("title"));
    review.setContent(rs.getString("content"));
    review.setHelpful(rs.getBoolean("helpful"));
    review.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
    review.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
    review.setAuthorName(rs.getString("author_name"));
    review.setAuthorAvatarUrl(rs.getString("author_avatar_url"));
    return review;
  }
}
