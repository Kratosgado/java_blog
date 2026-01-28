package com.kratosgado.blog.backend.repositories.jdbc;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.interfaces.HasId;

public abstract class CrudRepository<T extends HasId> extends ReadOnlyRepository<T> {

  public CrudRepository(Connection connection, Class<T> entityClass) {
    super(connection, entityClass);
  }

  public T save(T entity) {
    String columns = projectionMetadata.getInsertClause();
    String values = getColumnValues(entity);
    String query = "INSERT INTO " + tableName + columns + " VALUES ( " + values + ")";
    try (PreparedStatement statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
      statement.executeUpdate();
      try (ResultSet rs = statement.getGeneratedKeys()) {
        if (rs.next()) {
          entity.setId(rs.getLong(1));
        }
      }
    } catch (SQLException e) {
      throw BlogException.internal("Failed to save: " + entity + ": " + e.getMessage() + "\n" + query);
    }
    return entity;
  }

  public T update(T entity) {
    String query = "UPDATE " + tableName + " SET " + getUpdateClause(entity) + " WHERE id = ?";
    safeExecuteQuery(query, null, getValues(entity).toArray(new Object[0]), entity.getId());
    return entity;
  }

  public void deleteById(Long id) {
    String query = "DELETE FROM " + tableName + " WHERE id = ?";
    try (PreparedStatement statement = connection.prepareStatement(query)) {
      statement.setLong(1, id);
      statement.executeUpdate();
    } catch (SQLException e) {
      throw BlogException.internal("Failed to delete by id: " + id + ": " + e.getMessage());
    }
  }

  public void deleteAll() {
    String query = "DELETE FROM " + tableName;
    safeExecuteQuery(query, null);
  }

  private List<String> getValues(T entity) {
    return Arrays.stream(entity.getClass().getDeclaredFields())
        .filter(field -> {
          if (field.getName().equals("id")) {
            return false;
          }
          // Detect relationship fields: referencing HasId or List of HasId
          Class<?> type = field.getType();
          if (HasId.class.isAssignableFrom(type)) {
            return false;
          }
          if (Collection.class.isAssignableFrom(type)) {
            return false;
          }

          return true;
        })
        .map(field -> {
          try {
            field.setAccessible(true);
            var value = field.get(entity);
            return value == null ? "NULL" : "'" + value.toString() + "'";
          } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
          }
        })
        .collect(Collectors.toList());
  }

  private String getColumnValues(T entity) {
    return String.join(", ", getValues(entity));
  }

  private String getUpdateClause(T entity) {
    StringBuilder sb = new StringBuilder();
    for (Field field : entity.getClass().getDeclaredFields()) {
      if (field.getName().equals("id")) {
        continue;
      }
      try {
        field.setAccessible(true);
        Object value = field.get(entity);
        if (value != null) {
          sb.append(field.getName()).append(" = ?, ");
        }
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
    if (sb.length() > 0) {
      sb.deleteCharAt(sb.length() - 1);
    }
    return sb.toString();
  }
}
