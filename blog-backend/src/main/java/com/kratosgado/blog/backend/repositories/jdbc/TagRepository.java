package com.kratosgado.blog.backend.repositories.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.models.Tag;

@Repository

public class TagRepository extends SluggableRepository<Tag> {

  public TagRepository(DataSource dataSource) {
    super(dataSource, Tag.class);
    tableName = "tags";
  }

  @Override

  protected void initTable() {

    String sql = """
        CREATE TABLE IF NOT EXISTS tags (
            id BIGSERIAL PRIMARY KEY,
            name VARCHAR(50) UNIQUE NOT NULL,
            slug VARCHAR(50) UNIQUE NOT NULL,
            description TEXT
        );
        CREATE INDEX IF NOT EXISTS idx_tags_slug ON tags(slug);

        CREATE TABLE IF NOT EXISTS post_tags (
            post_id BIGINT REFERENCES posts(id) ON DELETE CASCADE,
            tag_id BIGINT REFERENCES tags(id) ON DELETE CASCADE,
            PRIMARY KEY (post_id, tag_id)
        );

        """;

    safeExecuteQuery(sql, null);

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

  public Optional<Tag> findByName(String name) {

    String query = "SELECT * FROM tags WHERE name = ?";

    return withConnection(conn -> {

      try (PreparedStatement statement = conn.prepareStatement(query)) {

        statement.setString(1, name);

        try (ResultSet rs = statement.executeQuery()) {

          if (rs.next()) {

            return Optional.of(toEntity(rs));

          }

        }

      } catch (SQLException e) {

        throw BlogException.internal("Failed to find tag by name: " + name + ": " + e.getMessage());

      }

      return Optional.empty();

    });

  }

  public boolean existsByName(String name) {

    String query = "SELECT COUNT(*) FROM tags WHERE name = ?";
    return withConnection(conn -> {
      try (PreparedStatement statement = conn.prepareStatement(query)) {
        statement.setString(1, name);
        try (ResultSet rs = statement.executeQuery()) {
          if (rs.next()) {
            return rs.getInt(1) > 0;
          }
        }
      } catch (SQLException e) {
        throw BlogException.internal("Failed to count tags by name: " + name + ": " + e.getMessage());
      }
      return false;
    });

  }

  public List<Tag> findByPostIds(List<Long> postIds) {

    String inClause = postIds.stream().map(i -> "?").reduce((a, b) -> a + ", " + b).orElse("?");

    String query = "SELECT t.* FROM tags t JOIN post_tags pt ON t.id = pt.tag_id WHERE pt.post_id IN (" + inClause

        + ")";

    return withConnection(conn -> {

      List<Tag> tags = new ArrayList<>();

      try (PreparedStatement statement = conn.prepareStatement(query)) {

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

    });

  }

  public List<Tag> findByPostId(Long postId) {

    String query = "SELECT t.* FROM tags t JOIN post_tags pt ON t.id = pt.tag_id WHERE pt.post_id = ?";

    return withConnection(conn -> {

      List<Tag> tags = new ArrayList<>();

      try (PreparedStatement statement = conn.prepareStatement(query)) {

        statement.setLong(1, postId);

        try (ResultSet rs = statement.executeQuery()) {

          while (rs.next()) {

            tags.add(toEntity(rs));

          }

        }

      } catch (SQLException e) {

        throw BlogException.internal("Failed to find tags by post ID: " + e.getMessage());

      }

      return tags;

    });

  }

  public void savePostTags(long postId, Long[] tagIds) {

    String query = "INSERT INTO post_tags (post_id, tag_id) VALUES (?, ?)";

    withConnection(conn -> {

      try (PreparedStatement statement = conn.prepareStatement(query)) {

        for (Long tagId : tagIds) {

          statement.setLong(1, postId);

          statement.setLong(2, tagId);

          statement.executeUpdate();

        }

      } catch (SQLException e) {

        throw BlogException.internal("Failed to save post tags: " + e.getMessage());

      }

      return null;

    });

  }

  public void deletePostTags(long postId) {

    String query = "DELETE FROM post_tags WHERE post_id = ?";

    safeExecuteQuery(query, null, postId);

  }

  public List<Tag> findAll(int size, int offset) {

    String query = "SELECT * FROM tags ORDER BY name ASC LIMIT ? OFFSET ?";

    return withConnection(conn -> {

      List<Tag> tags = new ArrayList<>();

      try (PreparedStatement statement = conn.prepareStatement(query)) {

        statement.setInt(1, size);

        statement.setInt(2, offset);

        try (ResultSet rs = statement.executeQuery()) {

          while (rs.next()) {

            tags.add(toEntity(rs));

          }

        }

      } catch (SQLException e) {

        throw BlogException.internal("Failed to find all tags: " + e.getMessage());

      }

      return tags;

    });

  }

  public List<Tag> searchByKeyword(String keyword, int size, int offset) {

    String query = "SELECT * FROM tags WHERE LOWER(name) LIKE ? ORDER BY name ASC LIMIT ? OFFSET ?";

    return withConnection(conn -> {

      List<Tag> tags = new ArrayList<>();

      try (PreparedStatement statement = conn.prepareStatement(query)) {

        statement.setString(1, "%" + keyword.toLowerCase() + "%");

        statement.setInt(2, size);

        statement.setInt(3, offset);

        try (ResultSet rs = statement.executeQuery()) {

          while (rs.next()) {

            tags.add(toEntity(rs));

          }

        }

      } catch (SQLException e) {

        throw BlogException.internal("Failed to search tags: " + e.getMessage());

      }

      return tags;

    });

  }

  public long countByKeyword(String keyword) {
    String query = "SELECT COUNT(*) FROM tags WHERE LOWER(name) LIKE ?";
    return withConnection(conn -> {
      try (PreparedStatement statement = conn.prepareStatement(query)) {
        statement.setString(1, "%" + keyword.toLowerCase() + "%");
        try (ResultSet rs = statement.executeQuery()) {
          if (rs.next()) {
            return rs.getLong(1);
          }
        }
      } catch (SQLException e) {
        throw BlogException.internal("Failed to count tags by keyword: " + e.getMessage());
      }
      return 0L;
    });
  }

}
