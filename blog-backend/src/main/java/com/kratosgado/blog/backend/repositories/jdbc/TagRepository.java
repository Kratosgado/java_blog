package com.kratosgado.blog.backend.repositories.jdbc;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.models.Tag;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TagRepository extends BaseRepository<Tag> {

  public TagRepository(Connection connection) {
    super(connection, Tag.class);
    tableName = "tags";
  }

  @Override
  public Tag toEntityFlat(ResultSet rs) throws SQLException {
    Tag tag = new Tag();
    tag.setId(rs.getLong("id"));
    tag.setName(rs.getString("name"));
    tag.setSlug(rs.getString("slug"));
    tag.setDescription(rs.getString("description"));
    return tag;
  }

  public Optional<Tag> findByName(String name) throws SQLException {
    String query = "SELECT * FROM tags WHERE name = ?";
    try (PreparedStatement statement = connection.prepareStatement(query)) {
      statement.setString(1, name);
      try (ResultSet rs = statement.executeQuery()) {
        if (rs.next()) {
          return Optional.of(toEntity(rs));
        }
      }
    }
    return Optional.empty();
  }

  public boolean existsByName(String name) throws SQLException {
    String query = "SELECT COUNT(*) FROM tags WHERE name = ?";
    try (PreparedStatement statement = connection.prepareStatement(query)) {
      statement.setString(1, name);
      try (ResultSet rs = statement.executeQuery()) {
        if (rs.next()) {
          return rs.getInt(1) > 0;
        }
      }
    }
    return false;
  }

  public List<Tag> findByPostIds(List<Long> postIds) {
    String inClause = postIds.stream().map(i -> "?").reduce((a, b) -> a + ", " + b).orElse("?");
    String query = "SELECT t.* FROM tags t JOIN post_tags pt ON t.id = pt.tag_id WHERE pt.post_id IN (" + inClause
        + ")";
    List<Tag> tags = new ArrayList<>();
    try (PreparedStatement statement = connection.prepareStatement(query)) {
      for (int i = 0; i < postIds.size(); i++) {
        statement.setObject(i + 1, postIds.get(i));
      }
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        tags.add(toEntity(rs));
      }
    } catch (SQLException e) {
      throw BlogException.internal("Failed to find tags by post IDs: " + e.getMessage());
    }
    return tags;
  }

  public List<Tag> findByPostId(Long postId) throws SQLException {
    String query = "SELECT t.* FROM tags t JOIN post_tags pt ON t.id = pt.tag_id WHERE pt.post_id = ?";
    List<Tag> tags = new ArrayList<>();
    try (PreparedStatement statement = connection.prepareStatement(query)) {
      statement.setLong(1, postId);
      try (ResultSet rs = statement.executeQuery()) {
        while (rs.next()) {
          tags.add(toEntity(rs));
        }
      }
    }
    return tags;
  }

  //
  // public Tag save(Tag tag) throws SQLException {
  // String query = "INSERT INTO tags (name, slug, description) VALUES (?, ?, ?)";
  // try (PreparedStatement statement = connection.prepareStatement(query,
  // PreparedStatement.RETURN_GENERATED_KEYS)) {
  // statement.setString(1, tag.getName());
  // statement.setString(2, tag.getSlug());
  // statement.setString(3, tag.getDescription());
  // statement.executeUpdate();
  // try (ResultSet rs = statement.getGeneratedKeys()) {
  // if (rs.next()) {
  // tag.setId(rs.getLong(1));
  // }
  // }
  // }
  // return tag;
  // }

  public void savePostTags(long postId, Long[] tagIds) {
    String query = "INSERT INTO post_tags (post_id, tag_id) VALUES (?, ?)";
    safeExecuteQuery(query, null, postId, (Object[]) tagIds);
  }

  public void deletePostTags(long postId) {
    String query = "DELETE FROM post_tags WHERE post_id = ?";
    safeExecuteQuery(query, null, postId);
  }

  public List<Tag> findAll(int size, int offset) {
    String query = "SELECT * FROM tags ORDER BY name ASC LIMIT ? OFFSET ?";
    List<Tag> tags = new ArrayList<>();
    safeExecuteQuery(query, rs -> {
      try {
        while (rs.next()) {
          tags.add(toEntity(rs));
        }
      } catch (SQLException e) {
        throw BlogException.internal("Failed to find all tags: " + e.getMessage());
      }
      return null;
    }, size, offset);
    return tags;
  }

  public List<Tag> searchByKeyword(String keyword, int size, int offset) {
    String query = "SELECT * FROM tags WHERE LOWER(name) LIKE ? ORDER BY name ASC LIMIT ? OFFSET ?";
    List<Tag> tags = new ArrayList<>();
    safeExecuteQuery(query, rs -> {
      try {
        while (rs.next()) {
          tags.add(toEntity(rs));
        }
      } catch (SQLException e) {
        throw BlogException.internal("Failed to search tags: " + e.getMessage());
      }
      return null;
    }, "%" + keyword.toLowerCase() + "%", size, offset);
    return tags;
  }

  public long countByKeyword(String keyword) {
    String query = "SELECT COUNT(*) FROM tags WHERE LOWER(name) LIKE ?";
    try (PreparedStatement statement = connection.prepareStatement(query)) {
      statement.setString(1, "%" + keyword.toLowerCase() + "%");
      try (ResultSet rs = statement.executeQuery()) {
        if (rs.next()) {
          return rs.getLong(1);
        }
      }
    } catch (SQLException e) {
      throw BlogException.internal("Failed to count tags by keyword: " + e.getMessage());
    }
    return 0;
  }

}
