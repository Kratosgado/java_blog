package com.kratosgado.blog.backend.repositories.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.models.User;

@Repository
public class UserRepository extends CrudRepository<User> {

  public UserRepository(DataSource dataSource) {
    super(dataSource, User.class);
    tableName = "users";
  }

  @Override
  protected void initTable() {
    String sql = """
        CREATE TABLE IF NOT EXISTS users (
            id BIGSERIAL PRIMARY KEY,
            username VARCHAR(50) UNIQUE NOT NULL,
            password VARCHAR(255) NOT NULL,
            email VARCHAR(100) UNIQUE NOT NULL,
            avatar_url VARCHAR(255),
            bio TEXT,
            website VARCHAR(255),
            location VARCHAR(100),
            role VARCHAR(20) DEFAULT 'USER'
        );
        CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
        CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
        """;
    safeExecuteQuery(sql, null);
  }

  @Override
  public User toEntityFlat(ResultSet rs) throws SQLException {
    User user = new User();
    user.setId(rs.getLong("id"));
    user.setUsername(rs.getString("username"));
    user.setPassword(rs.getString("password"));
    user.setEmail(rs.getString("email"));
    user.setAvatarUrl(rs.getString("avatar_url"));
    user.setBio(rs.getString("bio"));
    user.setWebsite(rs.getString("website"));
    user.setLocation(rs.getString("location"));
    user.setRole(rs.getString("role"));
    return user;
  }

  public Optional<User> findByEmail(String email) {
    String query = "SELECT * FROM users WHERE email = ?";
    return withConnection(conn -> {
      try (PreparedStatement statement = conn.prepareStatement(query)) {
        statement.setString(1, email);
        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
          return Optional.of(toEntity(rs));
        }
      } catch (SQLException e) {
        throw BlogException.internal("Failed to find by email: " + email + ": " + e.getMessage());
      }
      return Optional.empty();
    });
  }

  public Optional<User> findByUsername(String username) {
    String query = "SELECT * FROM users WHERE username = ?";
    return withConnection(conn -> {
      try (PreparedStatement statement = conn.prepareStatement(query)) {
        statement.setString(1, username);
        try (ResultSet rs = statement.executeQuery()) {
          if (rs.next()) {
            return Optional.of(toEntity(rs));
          }
        }
      } catch (SQLException e) {
        throw BlogException.internal("Failed to find by username: " + username + ": " + e.getMessage());
      }
      return Optional.empty();
    });
  }

  public boolean existsByEmail(String email) {
    String query = "SELECT COUNT(*) FROM users WHERE email = ?";
    return withConnection(conn -> {
      try (PreparedStatement statement = conn.prepareStatement(query)) {
        statement.setString(1, email);
        try (ResultSet rs = statement.executeQuery()) {
          if (rs.next()) {
            return rs.getInt(1) > 0;
          }
        }
      } catch (SQLException e) {
        throw BlogException.internal("Failed to count users by email: " + e.getMessage());
      }
      return false;
    });
  }

  public boolean existsByUsername(String username) {
    String query = "SELECT COUNT(*) FROM users WHERE username = ?";
    return withConnection(conn -> {
      try (PreparedStatement statement = conn.prepareStatement(query)) {
        statement.setString(1, username);
        try (ResultSet rs = statement.executeQuery()) {
          if (rs.next()) {
            return rs.getInt(1) > 0;
          }
        }
      } catch (SQLException e) {
        throw BlogException.internal("Failed to count users by username: " + e.getMessage());
      }
      return false;
    });
  }

  public java.util.List<User> findAll(int size, int offset, String sortBy, String sortDir) {
    String query = String.format("SELECT * FROM users ORDER BY %s %s LIMIT ? OFFSET ?", sortBy, sortDir);
    return withConnection(conn -> {
      java.util.List<User> users = new java.util.ArrayList<>();
      try (java.sql.PreparedStatement statement = conn.prepareStatement(query)) {
        statement.setInt(1, size);
        statement.setInt(2, offset);
        try (java.sql.ResultSet rs = statement.executeQuery()) {
          while (rs.next()) {
            users.add(toEntity(rs));
          }
        }
      } catch (java.sql.SQLException e) {
        throw BlogException.internal("Failed to find all users: " + e.getMessage());
      }
      return users;
    });
  }

  public long countAll() {
    String query = "SELECT COUNT(*) FROM users";
    return withConnection(conn -> {
      try (PreparedStatement statement = conn.prepareStatement(query);
          ResultSet rs = statement.executeQuery()) {
        if (rs.next()) {
          return rs.getLong(1);
        }
      } catch (SQLException e) {
        throw BlogException.internal("Failed to count users: " + e.getMessage());
      }
      return 0L;
    });
  }

}
