package com.kratosgado.blog.backend.repositories.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.LoggerFactory;
// import org.springframework.data.domain.Page; // removed unused import
import org.springframework.stereotype.Repository;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.enums.PostStatus;
import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.models.Tag;

@Repository
public class PostRepository extends SluggableRepository<Post> {

  final TagRepository tagRepository;

  public PostRepository(DataSource dataSource, TagRepository tagRepository, UserRepository userRepository,
      CategoryRepository categoryRepository) {
    super(dataSource, Post.class);
    tableName = "posts";
    this.tagRepository = tagRepository;
    registerRelationshipRepository("user", userRepository);
    registerRelationshipRepository("category", categoryRepository);
    registerManyToManyRelationship("tags", tagRepository, "post_tags", "post_id", "tag_id");
  }

  @Override
  protected void initTable() {
    String sql = """
        CREATE TABLE IF NOT EXISTS posts (
            id BIGSERIAL PRIMARY KEY,
            title VARCHAR(255) NOT NULL,
            slug VARCHAR(255) UNIQUE NOT NULL,
            content TEXT NOT NULL,
            excerpt TEXT,
            status VARCHAR(20) DEFAULT 'draft',
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            views INTEGER DEFAULT 0,
            likes_count INTEGER DEFAULT 0,
            cover_image VARCHAR(255),
            user_id BIGINT REFERENCES users(id),
            category_id BIGINT REFERENCES categories(id)
        );
        CREATE INDEX IF NOT EXISTS idx_posts_slug ON posts(slug);
        CREATE INDEX IF NOT EXISTS idx_posts_status ON posts(status);
        CREATE INDEX IF NOT EXISTS idx_posts_created_at ON posts(created_at);
        """;
    safeExecuteQuery(sql, null);
  }

  @Override
  public Post toEntityFlat(ResultSet rs) throws SQLException {
    Post post = new Post();
    post.setId(rs.getLong("id"));
    post.setTitle(rs.getString("title"));
    post.setSlug(rs.getString("slug"));
    post.setContent(rs.getString("content"));
    post.setExcerpt(rs.getString("excerpt"));
    post.setStatus(PostStatus.valueOf(rs.getString("status")));
    post.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
    post.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
    post.setViews(rs.getInt("views"));
    post.setLikesCount(rs.getInt("likes_count"));
    post.setCoverImage(rs.getString("cover_image"));
    post.setUserId(rs.getLong("user_id"));
    post.setCategoryId(rs.getLong("category_id"));
    return post;
  }

  public List<Post> findPublishedPosts(int size, int offset) {
    return executePagedSelect("WHERE t.status = 'published'", "ORDER BY t.created_at DESC", size, offset);
  }

  public void incrementViews(String slug) {
    String query = "UPDATE posts SET views = views + 1 WHERE slug = ?";
    safeExecuteQuery(query, null, slug);
  }

  public long countPublishedPosts() {
    String query = "SELECT COUNT(*) FROM posts WHERE status = 'published'";
    return withConnection(conn -> {
      try (PreparedStatement statement = conn.prepareStatement(query);
          ResultSet rs = statement.executeQuery()) {
        if (rs.next()) {
          return rs.getLong(1);
        }
      } catch (SQLException e) {
        throw BlogException.internal("Failed to count published posts: " + e.getMessage());
      }
      return 0L;
    });
  }

  public List<Post> searchPostsByKeyword(String keyword, int size, int offset) {
    String like = "%" + keyword.toLowerCase() + "%";
    return executePagedSelect(
        "WHERE (LOWER(t.title) LIKE ? OR LOWER(t.content) LIKE ?) AND t.status = 'published'",
        "ORDER BY t.created_at DESC",
        size, offset,
        like, like);
  }

  public long countPostsByKeyword(String keyword) {
    String query = "SELECT COUNT(*) FROM posts WHERE (LOWER(title) LIKE ? OR LOWER(content) LIKE ?) AND status = 'published'";
    return withConnection(conn -> {
      try (PreparedStatement statement = conn.prepareStatement(query)) {
        statement.setString(1, "%" + keyword.toLowerCase() + "%");
        statement.setString(2, "%" + keyword.toLowerCase() + "%");
        try (ResultSet rs = statement.executeQuery()) {
          if (rs.next()) {
            return rs.getLong(1);
          }
        }
      } catch (SQLException e) {
        throw BlogException.internal("Failed to count posts by keyword: " + e.getMessage());
      }
      return 0L;
    });
  }

  public List<Post> findPostsByUser(Long userId, int size, int offset) {
    return executePagedSelect("WHERE t.user_id = ?", "ORDER BY t.created_at DESC", size, offset, userId);
  }

  public long countPostsByUser(Long userId) {
    String query = "SELECT COUNT(*) FROM posts WHERE user_id = ?";
    return withConnection(conn -> {
      try (PreparedStatement statement = conn.prepareStatement(query)) {
        statement.setLong(1, userId);
        try (ResultSet rs = statement.executeQuery()) {
          if (rs.next()) {
            return rs.getLong(1);
          }
        }
      } catch (SQLException e) {
        throw BlogException.internal("Failed to count user posts: " + e.getMessage());
      }
      return 0L;
    });
  }

  public List<Post> findPostsByCategory(Long categoryId, int size, int offset) {
    return executePagedSelect("WHERE t.category_id = ? AND t.status = 'published'", "ORDER BY t.created_at DESC", size,
        offset, categoryId);
  }

  public long countPostsByCategory(Long categoryId) {
    String query = "SELECT COUNT(*) FROM posts WHERE category_id = ? AND status = 'published'";
    return withConnection(conn -> {
      try (PreparedStatement statement = conn.prepareStatement(query)) {
        statement.setLong(1, categoryId);
        try (ResultSet rs = statement.executeQuery()) {
          if (rs.next()) {
            return rs.getLong(1);
          }
        }
      } catch (SQLException e) {
        throw BlogException.internal("Failed to count posts by category: " + e.getMessage());
      }
      return 0L;
    });
  }

  public List<Post> findLatestPosts(int limit) {
    return executePagedSelect(null, "ORDER BY t.created_at DESC", limit, 0);
  }

  public List<Post> findTopPostsByViews(int limit) {
    return executePagedSelect("WHERE t.status = 'published'", "ORDER BY t.views DESC", limit, 0);
  }

  public long countAll() {
    String query = "SELECT COUNT(*) FROM posts";
    return withConnection(conn -> {
      try (PreparedStatement statement = conn.prepareStatement(query);
          ResultSet rs = statement.executeQuery()) {
        if (rs.next()) {
          return rs.getLong(1);
        }
      } catch (SQLException e) {
        throw BlogException.internal("Failed to count posts: " + e.getMessage());
      }
      return 0L;
    });
  }

  public long countByStatus(PostStatus status) {
    String query = "SELECT COUNT(*) FROM posts WHERE status = ?";
    return withConnection(conn -> {
      try (PreparedStatement statement = conn.prepareStatement(query)) {
        statement.setString(1, status.name());
        try (ResultSet rs = statement.executeQuery()) {
          if (rs.next()) {
            return rs.getLong(1);
          }
        }
      } catch (SQLException e) {
        throw BlogException.internal("Failed to count posts by status: " + e.getMessage());
      }
      return 0L;
    });
  }

  public long sumViewsByUserId(Long userId) {
    String query = "SELECT SUM(views) FROM posts WHERE user_id = ?";
    return withConnection(conn -> {
      try (PreparedStatement statement = conn.prepareStatement(query)) {
        statement.setLong(1, userId);
        try (ResultSet rs = statement.executeQuery()) {
          if (rs.next()) {
            return rs.getLong(1);
          }
        }
      } catch (SQLException e) {
        throw BlogException.internal("Failed to sum views: " + e.getMessage());
      }
      return 0L;
    });
  }

}
