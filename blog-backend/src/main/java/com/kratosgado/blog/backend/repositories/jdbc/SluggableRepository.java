package com.kratosgado.blog.backend.repositories.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.interfaces.HasId;

public abstract class SluggableRepository<T extends HasId> extends CrudRepository<T> {

  public SluggableRepository(Connection connection, Class<T> entityClass) {
    super(connection, entityClass);
  }

  public Optional<T> findBySlug(String slug) {
    String query = generateSelectQuery("WHERE t.slug = ?");
    List<T> results = executeSelect(query, slug);
    return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
  }

  public boolean existsBySlug(String slug) {
    String query = "SELECT COUNT(*) FROM " + tableName + " WHERE slug = ?";
    try (PreparedStatement statement = connection.prepareStatement(query)) {
      statement.setString(1, slug);
      try (ResultSet rs = statement.executeQuery()) {
        if (rs.next()) {
          return rs.getInt(1) > 0;
        }
      }
    } catch (SQLException e) {
      throw BlogException.internal("Failed to count records: " + e.getMessage());
    }
    return false;
  }
}
