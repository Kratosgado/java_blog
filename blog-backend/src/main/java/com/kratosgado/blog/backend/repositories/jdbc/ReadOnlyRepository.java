package com.kratosgado.blog.backend.repositories.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.interfaces.HasId;

import java.sql.Connection;

public abstract class ReadOnlyRepository<T extends HasId> extends BaseRepository<T> {

  public ReadOnlyRepository(Connection connection, Class<T> entityClass) {
    super(connection, entityClass);
  }

  public List<T> findAll() {
    String query = generateSelectQuery("");
    return executeSelect(query);
  }

  public List<T> findAll(int limit, int offset) {
    String query = generateSelectQuery("LIMIT ? OFFSET ?");
    return executeSelect(query, limit, offset);
  }

  public List<T> findAllByIds(List<Long> ids) {
    if (ids == null || ids.isEmpty())
      return new ArrayList<>();
    String inClause = ids.stream().map(i -> "?").collect(Collectors.joining(", "));
    String query = generateSelectQuery("WHERE t.id IN (" + inClause + ")");
    return executeSelect(query, ids.toArray());
  }

  public Optional<T> findById(Long id) {
    String query = generateSelectQuery("WHERE t.id = ?");
    List<T> results = executeSelect(query, id);
    return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
  }

  public boolean existsById(Long id) {
    String query = "SELECT COUNT(*) FROM " + tableName + " WHERE id = ?";
    try (PreparedStatement statement = connection.prepareStatement(query)) {
      statement.setLong(1, id);
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

  public Long count() {
    String query = "SELECT COUNT(*) FROM " + tableName;
    final long[] count = { 0L };
    safeExecuteQuery(query, rs -> {
      try {
        if (rs.next()) {
          count[0] = rs.getLong(1);
        }
      } catch (SQLException e) {
        throw BlogException.internal("Failed to count records: " + e.getMessage());
      }
      return null;
    });
    return count[0];
  }
}
