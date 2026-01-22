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
import com.kratosgado.blog.models.Post;

import jakarta.annotation.PostConstruct;

@Repository
@DependsOn("databaseConfig")
public class PostDAO extends BaseDAO {
  private static final Logger logger = LoggerFactory.getLogger(PostDAO.class);

  @Autowired
  private DatabaseConfig databaseConfig;

  @PostConstruct
  @Override
  protected void initDatabase() {
    try (Connection conn = DatabaseConfig.getConnection();
        Statement stmt = conn.createStatement();) {
      String sql = """
                CREATE TABLE IF NOT EXISTS posts (
            id SERIAL PRIMARY KEY,
            user_id INTEGER NOT NULL,
            category_id INTEGER,
            title VARCHAR(255) NOT NULL,
            slug VARCHAR(255) UNIQUE,
            content TEXT NOT NULL,
            excerpt VARCHAR(500),
            status VARCHAR(20) DEFAULT 'draft' CHECK (status IN ('draft', 'published', 'archived')),
            cover_image VARCHAR(500),
            views INTEGER DEFAULT 0,
            likes_count INTEGER DEFAULT 0,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

            CONSTRAINT chk_title_not_empty CHECK (LENGTH(TRIM(title)) > 0),
            CONSTRAINT chk_content_not_empty CHECK (LENGTH(TRIM(content)) > 0),
            CONSTRAINT chk_views_positive CHECK (views >= 0),
            CONSTRAINT chk_likes_positive CHECK (likes_count >= 0)
          );
                """;
      stmt.executeUpdate(sql);

      logger.debug("Posts table initialized successfully");
      createIndexes(conn);
    } catch (Exception e) {
      logger.error("Failed to initialize posts table", e);
    }
  }

  private void createIndexes(Connection conn) {
    var sql = """
        CREATE INDEX IF NOT EXISTS idx_posts_user_id ON posts(user_id);
        CREATE INDEX IF NOT EXISTS idx_posts_category_id ON posts(category_id);
        CREATE INDEX IF NOT EXISTS idx_posts_status ON posts(status);
        CREATE INDEX IF NOT EXISTS idx_posts_title ON posts(title);
        CREATE INDEX IF NOT EXISTS idx_posts_slug ON posts(slug);
        CREATE INDEX IF NOT EXISTS idx_posts_created_at ON posts(created_at);
        """;
    try (Statement stmt = conn.createStatement()) {
      stmt.executeUpdate(sql);
      logger.debug("Database indexes created successfully");
    } catch (Exception e) {
      logger.error("Failed to create indexes", e);
    }
  }

  public Optional<Post> createPost(Post post) {
    String sql = "INSERT INTO posts (user_id, category_id, title, slug, content, excerpt, status, cover_image) VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setLong(1, post.getUserId());
      if (post.getCategoryId() != null) {
        stmt.setInt(2, post.getCategoryId());
      } else {
        stmt.setNull(2, java.sql.Types.INTEGER);
      }
      stmt.setString(3, post.getTitle());
      stmt.setString(4, post.getSlug());
      stmt.setString(5, post.getContent());
      stmt.setString(6, post.getExcerpt());
      stmt.setString(7, post.getStatus());
      stmt.setString(8, post.getCoverImage());
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        post.setId(rs.getInt("id"));
        logger.info("Post created successfully: {} with ID: {}", post.getTitle(), post.getId());
        return Optional.of(post);
      }
      return Optional.empty();
    } catch (Exception e) {
      logger.error("Failed to create post: {}", post.getTitle(), e);
      return Optional.empty();
    }
  }

  public Optional<Post> updatePost(Post post) {
    String sql = "UPDATE posts SET category_id = ?, title = ?, slug = ?, content = ?, excerpt = ?, status = ?, cover_image = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      if (post.getCategoryId() != null) {
        stmt.setInt(1, post.getCategoryId());
      } else {
        stmt.setNull(1, java.sql.Types.INTEGER);
      }
      stmt.setString(2, post.getTitle());
      stmt.setString(3, post.getSlug());
      stmt.setString(4, post.getContent());
      stmt.setString(5, post.getExcerpt());
      stmt.setString(6, post.getStatus());
      stmt.setString(7, post.getCoverImage());
      stmt.setInt(8, post.getId());
      int updated = stmt.executeUpdate();
      if (updated > 0) {
        logger.info("Post updated successfully: {}", post.getId());
        return Optional.of(post);
      }
      return Optional.empty();
    } catch (Exception e) {
      logger.error("Failed to update post: {}", post.getId(), e);
      return Optional.empty();
    }
  }

  public boolean deletePost(Integer id) {
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

  public Optional<Post> getPostById(Integer id) {
    String sql = "SELECT p.*, u.username as author_name, u.avatar_url as author_avatar_url, c.name as category_name FROM posts p "
        +
        "LEFT JOIN users u ON p.user_id = u.id " +
        "LEFT JOIN categories c ON p.category_id = c.id " +
        "WHERE p.id = ?";
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

  public Optional<Post> getPostBySlug(String slug) {
    String sql = "SELECT p.*, u.username as author_name, u.avatar_url as author_avatar_url, c.name as category_name FROM posts p "
        +
        "LEFT JOIN users u ON p.user_id = u.id " +
        "LEFT JOIN categories c ON p.category_id = c.id " +
        "WHERE p.slug = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setString(1, slug);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return Optional.of(mapResultSetToPost(rs));
      }
      return Optional.empty();
    } catch (Exception e) {
      logger.error("Failed to fetch post by slug: {}", slug, e);
      return Optional.empty();
    }
  }

  public List<Post> getPostsByUserId(Integer userId) {
    String sql = "SELECT p.*, u.username as author_name, u.avatar_url as author_avatar_url, c.name as category_name FROM posts p "
        +
        "LEFT JOIN users u ON p.user_id = u.id " +
        "LEFT JOIN categories c ON p.category_id = c.id " +
        "WHERE p.user_id = ? ORDER BY p.created_at DESC";
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
    String sql = "SELECT p.*, u.username as author_name, u.avatar_url as author_avatar_url, c.name as category_name FROM posts p "
        +
        "LEFT JOIN users u ON p.user_id = u.id " +
        "LEFT JOIN categories c ON p.category_id = c.id " +
        "WHERE p.status = ? ORDER BY p.created_at DESC";
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
    String sql = "SELECT p.*, u.username as author_name, u.avatar_url as author_avatar_url, c.name as category_name FROM posts p "
        +
        "LEFT JOIN users u ON p.user_id = u.id " +
        "LEFT JOIN categories c ON p.category_id = c.id " +
        "ORDER BY p.created_at DESC";
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
    String sql = "SELECT p.*, u.username as author_name, u.avatar_url as author_avatar_url, c.name as category_name FROM posts p "
        +
        "LEFT JOIN users u ON p.user_id = u.id " +
        "LEFT JOIN categories c ON p.category_id = c.id " +
        "WHERE (LOWER(p.title) LIKE LOWER(?) OR LOWER(p.content) LIKE LOWER(?)) " +
        "AND p.status = 'published' " +
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

  public List<Post> getPostsByTag(String tagName) {
    String sql = "SELECT DISTINCT p.*, u.username as author_name, u.avatar_url as author_avatar_url, c.name as category_name "
        +
        "FROM posts p " +
        "LEFT JOIN users u ON p.user_id = u.id " +
        "LEFT JOIN categories c ON p.category_id = c.id " +
        "LEFT JOIN post_tags pt ON p.id = pt.post_id " +
        "LEFT JOIN tags t ON pt.tag_id = t.id " +
        "WHERE LOWER(t.name) LIKE LOWER(?) AND p.status = 'published' " +
        "ORDER BY p.id DESC";
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

  public List<Post> getPostsByCategoryId(Integer categoryId) {
    String sql = "SELECT p.*, u.username as author_name, u.avatar_url as author_avatar_url, c.name as category_name FROM posts p "
        +
        "LEFT JOIN users u ON p.user_id = u.id " +
        "LEFT JOIN categories c ON p.category_id = c.id " +
        "WHERE p.category_id = ? " +
        "ORDER BY p.created_at DESC";
    List<Post> posts = new ArrayList<>();
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, categoryId);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        posts.add(mapResultSetToPost(rs));
      }
      logger.info("Found {} posts for category ID: {}", posts.size(), categoryId);
    } catch (Exception e) {
      logger.error("Failed to fetch posts by category ID: {}", categoryId, e);
    }
    return posts;
  }

  public List<Post> getPostsPaginated(int pageNumber, int pageSize) {
    String sql = "SELECT p.*, u.username as author_name, u.avatar_url as author_avatar_url, c.name as category_name FROM posts p "
        +
        "LEFT JOIN users u ON p.user_id = u.id " +
        "LEFT JOIN categories c ON p.category_id = c.id " +
        "WHERE p.status = 'published' " +
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

  public List<Post> getPostsByUserIdPaginated(Integer userId, int pageNumber, int pageSize) {
    String sql = "SELECT p.*, u.username as author_name, u.avatar_url as author_avatar_url, c.name as category_name FROM posts p "
        +
        "LEFT JOIN users u ON p.user_id = u.id " +
        "LEFT JOIN categories c ON p.category_id = c.id " +
        "WHERE p.user_id = ? " +
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

  public boolean incrementViews(Integer postId) {
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

  public boolean incrementLikesCount(Integer postId) {
    String sql = "UPDATE posts SET likes_count = likes_count + 1 WHERE id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, postId);
      int updated = stmt.executeUpdate();
      if (updated > 0) {
        logger.info("Likes count incremented for post id: {}", postId);
        return true;
      }
      return false;
    } catch (Exception e) {
      logger.error("Failed to increment likes count for post id: {}", postId, e);
      return false;
    }
  }

  public boolean decrementLikesCount(Integer postId) {
    String sql = "UPDATE posts SET likes_count = GREATEST(likes_count - 1, 0) WHERE id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, postId);
      int updated = stmt.executeUpdate();
      if (updated > 0) {
        logger.info("Likes count decremented for post id: {}", postId);
        return true;
      }
      return false;
    } catch (Exception e) {
      logger.error("Failed to decrement likes count for post id: {}", postId, e);
      return false;
    }
  }

  public boolean addTagToPost(Integer postId, Integer tagId) {
    String sql = "INSERT INTO post_tags (post_id, tag_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, postId);
      stmt.setInt(2, tagId);
      int inserted = stmt.executeUpdate();
      if (inserted > 0) {
        logger.info("Tag {} added to post {}", tagId, postId);
        return true;
      }
      return false;
    } catch (Exception e) {
      logger.error("Failed to add tag {} to post {}", tagId, postId, e);
      return false;
    }
  }

  public boolean removeTagFromPost(Integer postId, Integer tagId) {
    String sql = "DELETE FROM post_tags WHERE post_id = ? AND tag_id = ?";
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);) {
      stmt.setInt(1, postId);
      stmt.setInt(2, tagId);
      int deleted = stmt.executeUpdate();
      if (deleted > 0) {
        logger.info("Tag {} removed from post {}", tagId, postId);
        return true;
      }
      return false;
    } catch (Exception e) {
      logger.error("Failed to remove tag {} from post {}", tagId, postId, e);
      return false;
    }
  }

  private Post mapResultSetToPost(ResultSet rs) throws Exception {
    Post post = new Post();
    post.setId(rs.getInt("id"));
    post.setUserId(rs.getLong("user_id"));

    int categoryId = rs.getInt("category_id");
    if (!rs.wasNull()) {
      post.setCategoryId(categoryId);
    }

    post.setTitle(rs.getString("title"));
    post.setSlug(rs.getString("slug"));
    post.setContent(rs.getString("content"));
    post.setExcerpt(rs.getString("excerpt"));
    post.setStatus(rs.getString("status"));
    post.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
    post.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
    post.setViews(rs.getInt("views"));
    post.setLikesCount(rs.getInt("likes_count"));
    post.setCoverImage(rs.getString("cover_image"));
    post.setAuthorName(rs.getString("author_name"));
    post.setAuthorAvatarUrl(rs.getString("author_avatar_url"));
    post.setCategoryName(rs.getString("category_name"));
    return post;
  }
}
