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
import com.kratosgado.blog.utils.cache.PostCache;
import com.kratosgado.blog.utils.interfaces.DAO;

public class PostDAO extends DAO {
  private static final Logger logger = LoggerFactory.getLogger(PostDAO.class);

  public PostDAO() {
    initDatabase();
  }

  @Override
  protected void initDatabase() {
    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement();) {
      String sql = """
                CREATE TABLE posts (
            id SERIAL PRIMARY KEY,
            user_id INTEGER NOT NULL,
            title VARCHAR(255) NOT NULL,
            content TEXT NOT NULL,
            excerpt VARCHAR(500),
            status VARCHAR(20) DEFAULT 'draft' CHECK (status IN ('draft', 'published', 'archived')),
            featured_image VARCHAR(500),
            cover_image VARCHAR(500),
            icon VARCHAR(500),
            views INTEGER DEFAULT 0,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

            CONSTRAINT chk_title_not_empty CHECK (LENGTH(TRIM(title)) > 0),
            CONSTRAINT chk_content_not_empty CHECK (LENGTH(TRIM(content)) > 0),
            CONSTRAINT chk_views_positive CHECK (views >= 0)
          );

          -- Performance indexes for frequent queries
          CREATE INDEX idx_posts_user_id ON posts(user_id);
          CREATE INDEX idx_posts_status ON posts(status);
          CREATE INDEX idx_posts_title ON posts(title);
          CREATE INDEX idx_posts_created_at ON posts(created_at DESC);

          COMMENT ON TABLE posts IS 'Stores blog posts with full content and metadata';
          COMMENT ON COLUMN posts.status IS 'Post publication status: draft, published, or archived';
          COMMENT ON COLUMN posts.icon IS 'Small icon/thumbnail for the post';
          COMMENT ON COLUMN posts.cover_image IS 'Large banner/cover image for the post';
                """;
      stmt.executeUpdate(sql);
      logger.debug("Posts table initialized successfully");

      // Create indexes for performance optimization
      createIndexes(conn);
    } catch (Exception e) {
      logger.error("Failed to initialize posts table", e);
    }
  }

  private void createIndexes(Connection conn) {
    try (Statement stmt = conn.createStatement()) {
      // Index on user_id for quick user post retrieval
      stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_posts_user_id ON posts(user_id)");
      // Index on status for quick filtering
      stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_posts_status ON posts(status)");
      // Index on title for search operations
      stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_posts_title ON posts(title)");
      // Index on created_at for date-based queries
      stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_posts_created_at ON posts(created_at)");
      logger.debug("Database indexes created successfully");
    } catch (Exception e) {
      logger.error("Failed to create indexes", e);
    }
  }

  public boolean createPost(Post post) {
    String sql = "INSERT INTO posts (user_id, title, content, excerpt, status, featured_image, cover_image, icon) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, post.getUserId());
      stmt.setString(2, post.getTitle());
      stmt.setString(3, post.getContent());
      stmt.setString(4, post.getExcerpt());
      stmt.setString(5, post.getStatus());
      stmt.setString(6, post.getFeaturedImage());
      stmt.setString(7, post.getCoverImage());
      stmt.setString(8, post.getIcon());
      stmt.executeUpdate();
      logger.info("Post created successfully: {}", post.getTitle());
      return true;
    } catch (Exception e) {
      logger.error("Failed to create post: {}", post.getTitle(), e);
      return false;
    }
  }

  public boolean updatePost(Post post) {
    String sql = "UPDATE posts SET title = ?, content = ?, excerpt = ?, status = ?, featured_image = ?, cover_image = ?, icon = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setString(1, post.getTitle());
      stmt.setString(2, post.getContent());
      stmt.setString(3, post.getExcerpt());
      stmt.setString(4, post.getStatus());
      stmt.setString(5, post.getFeaturedImage());
      stmt.setString(6, post.getCoverImage());
      stmt.setString(7, post.getIcon());
      stmt.setInt(8, post.getId());
      stmt.executeUpdate();
      // Invalidate cache for this post
      PostCache.getInstance().invalidate(post.getId());
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
      // Invalidate cache
      PostCache.getInstance().invalidate(id);
      logger.info("Post deleted successfully: {}", id);
      return true;
    } catch (Exception e) {
      logger.error("Failed to delete post: {}", id, e);
      return false;
    }
  }

  public Optional<Post> getPostById(int id) {
    // Check cache first
    Optional<Post> cached = PostCache.getInstance().get(id);
    if (cached.isPresent()) {
      return cached;
    }

    String sql = "SELECT p.*, u.username as author_name, u.avatar_url as author_avatar_url FROM posts p " +
        "LEFT JOIN users u ON p.user_id = u.id WHERE p.id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, id);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        Post post = mapResultSetToPost(rs);
        PostCache.getInstance().put(id, post);
        return Optional.of(post);
      }
      return Optional.empty();
    } catch (Exception e) {
      logger.error("Failed to fetch post by id: {}", id, e);
      return Optional.empty();
    }
  }

  public List<Post> getPostsByUserId(int userId) {
    String sql = "SELECT p.*, u.username as author_name, u.avatar_url as author_avatar_url FROM posts p " +
        "LEFT JOIN users u ON p.user_id = u.id WHERE p.user_id = ? ORDER BY p.created_at DESC";
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
    String sql = "SELECT p.*, u.username as author_name, u.avatar_url as author_avatar_url FROM posts p " +
        "LEFT JOIN users u ON p.user_id = u.id WHERE p.status = ? ORDER BY p.created_at DESC";
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
    String sql = "SELECT p.*, u.username as author_name, u.avatar_url as author_avatar_url FROM posts p " +
        "LEFT JOIN users u ON p.user_id = u.id ORDER BY p.created_at DESC";
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

  public List<Post> searchPostsByKeyword(String keyword) {
    String sql = "SELECT p.*, u.username as author_name, u.avatar_url as author_avatar_url FROM posts p " +
        "LEFT JOIN users u ON p.user_id = u.id " +
        "WHERE LOWER(p.title) LIKE LOWER(?) OR LOWER(p.content) LIKE LOWER(?) " +
        "ORDER BY p.created_at DESC";
    List<Post> posts = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      String searchPattern = "%" + keyword + "%";
      stmt.setString(1, searchPattern);
      stmt.setString(2, searchPattern);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        posts.add(mapResultSetToPost(rs));
      }
      logger.info("Keyword search found {} posts for: {}", posts.size(), keyword);
    } catch (Exception e) {
      logger.error("Failed to search posts by keyword: {}", keyword, e);
    }
    return posts;
  }

  public List<Post> searchPostsByAuthor(String authorName) {
    String sql = "SELECT p.*, u.username as author_name, u.avatar_url as author_avatar_url FROM posts p " +
        "LEFT JOIN users u ON p.user_id = u.id " +
        "WHERE LOWER(u.username) LIKE LOWER(?) " +
        "ORDER BY p.created_at DESC";
    List<Post> posts = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      String searchPattern = "%" + authorName + "%";
      stmt.setString(1, searchPattern);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        posts.add(mapResultSetToPost(rs));
      }
      logger.info("Author search found {} posts for: {}", posts.size(), authorName);
    } catch (Exception e) {
      logger.error("Failed to search posts by author: {}", authorName, e);
    }
    return posts;
  }

  public List<Post> getPostsByTag(String tagName) {
    String sql = "SELECT DISTINCT p.*, u.username as author_name, u.avatar_url as author_avatar_url FROM posts p " +
        "LEFT JOIN users u ON p.user_id = u.id " +
        "LEFT JOIN post_tags pt ON p.id = pt.post_id " +
        "LEFT JOIN tags t ON pt.tag_id = t.id " +
        "WHERE LOWER(t.name) LIKE LOWER(?) " +
        "ORDER BY p.created_at DESC";
    List<Post> posts = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      String searchPattern = "%" + tagName + "%";
      stmt.setString(1, searchPattern);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        posts.add(mapResultSetToPost(rs));
      }
      logger.info("Tag search found {} posts for tag: {}", posts.size(), tagName);
    } catch (Exception e) {
      logger.error("Failed to search posts by tag: {}", tagName, e);
    }
    return posts;
  }

  /**
   * Get paginated posts with efficient LIMIT/OFFSET
   * 
   * @param pageNumber 1-based page number
   * @param pageSize   number of posts per page
   * @return list of posts for the specified page
   */
  public List<Post> getPostsPaginated(int pageNumber, int pageSize) {
    String sql = "SELECT p.*, u.username as author_name, u.avatar_url as author_avatar_url FROM posts p " +
        "LEFT JOIN users u ON p.user_id = u.id WHERE p.status = 'published' " +
        "ORDER BY p.created_at DESC LIMIT ? OFFSET ?";
    List<Post> posts = new ArrayList<>();
    int offset = (pageNumber - 1) * pageSize;
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, pageSize);
      stmt.setInt(2, offset);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        posts.add(mapResultSetToPost(rs));
      }
      logger.debug("Fetched {} posts for page {}", posts.size(), pageNumber);
    } catch (Exception e) {
      logger.error("Failed to fetch paginated posts for page: {}", pageNumber, e);
    }
    return posts;
  }

  /**
   * Get total count of published posts for pagination metadata
   */
  public int getPublishedPostCount() {
    String sql = "SELECT COUNT(*) as total FROM posts WHERE status = 'published'";
    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement();) {
      ResultSet rs = stmt.executeQuery(sql);
      if (rs.next()) {
        return rs.getInt("total");
      }
    } catch (Exception e) {
      logger.error("Failed to get published post count", e);
    }
    return 0;
  }

  /**
   * Get paginated posts by user with LIMIT/OFFSET
   */
  public List<Post> getPostsByUserIdPaginated(int userId, int pageNumber, int pageSize) {
    String sql = "SELECT p.*, u.username as author_name, u.avatar_url as author_avatar_url FROM posts p " +
        "LEFT JOIN users u ON p.user_id = u.id WHERE p.user_id = ? " +
        "ORDER BY p.created_at DESC LIMIT ? OFFSET ?";
    List<Post> posts = new ArrayList<>();
    int offset = (pageNumber - 1) * pageSize;
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, userId);
      stmt.setInt(2, pageSize);
      stmt.setInt(3, offset);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        posts.add(mapResultSetToPost(rs));
      }
      logger.debug("Fetched {} posts for user {} page {}", posts.size(), userId, pageNumber);
    } catch (Exception e) {
      logger.error("Failed to fetch paginated posts for user: {}", userId, e);
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
    Post post = new Post();
    post.setId(rs.getInt("id"));
    post.setUserId(rs.getInt("user_id"));
    post.setTitle(rs.getString("title"));
    post.setContent(rs.getString("content"));
    post.setExcerpt(rs.getString("excerpt"));
    post.setStatus(rs.getString("status"));
    post.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
    post.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
    post.setViews(rs.getInt("views"));
    post.setFeaturedImage(rs.getString("featured_image"));
    post.setCoverImage(rs.getString("cover_image"));
    post.setIcon(rs.getString("icon"));
    post.setAuthorName(rs.getString("author_name"));
    post.setAuthorAvatarUrl(rs.getString("author_avatar_url"));
    return post;
  }
}
