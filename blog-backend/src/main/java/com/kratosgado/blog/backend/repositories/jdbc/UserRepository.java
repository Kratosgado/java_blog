package com.kratosgado.blog.backend.repositories.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.models.User;

@Repository
public class UserRepository extends CrudRepository<User> {

  public UserRepository(Connection connection) {
    super(connection, User.class);
    tableName = "users";
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
    try (PreparedStatement statement = connection.prepareStatement(query)) {
      statement.setString(1, email);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        return Optional.of(toEntity(rs));
      }
    } catch (SQLException e) {
      throw BlogException.internal("Failed to find by email: " + email + ": " + e.getMessage());
    }
    return Optional.empty();
  }

  public Optional<User> findByUsername(String username) throws SQLException {
    String query = "SELECT * FROM users WHERE username = ?";
    try (PreparedStatement statement = connection.prepareStatement(query)) {
      statement.setString(1, username);
      try (ResultSet rs = statement.executeQuery()) {
        if (rs.next()) {
          return Optional.of(toEntity(rs));
        }
      }
    }
    return Optional.empty();
  }

  public boolean existsByEmail(String email) throws SQLException {
    String query = "SELECT COUNT(*) FROM users WHERE email = ?";
    try (PreparedStatement statement = connection.prepareStatement(query)) {
      statement.setString(1, email);
      try (ResultSet rs = statement.executeQuery()) {
        if (rs.next()) {
          return rs.getInt(1) > 0;
        }
      }
    }
    return false;
  }

  public boolean existsByUsername(String username) throws SQLException {
    String query = "SELECT COUNT(*) FROM users WHERE username = ?";
    try (PreparedStatement statement = connection.prepareStatement(query)) {
      statement.setString(1, username);
      try (ResultSet rs = statement.executeQuery()) {
        if (rs.next()) {
          return rs.getInt(1) > 0;
        }
      }
    }
    return false;
  }

}
