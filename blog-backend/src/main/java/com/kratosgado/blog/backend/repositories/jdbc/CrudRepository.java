package com.kratosgado.blog.backend.repositories.jdbc;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.interfaces.HasId;

public abstract class CrudRepository<T extends HasId> extends ReadOnlyRepository<T> {

  public CrudRepository(DataSource dataSource, Class<T> entityClass) {
    super(dataSource, entityClass);
  }

  public T save(T entity) {
    String columns = projectionMetadata.getInsertClause();
    String values = getColumnValues(entity);
    String query = "INSERT INTO " + tableName + columns + " VALUES ( " + values + ")";

    return withConnection(conn -> {
      try (PreparedStatement statement = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
        statement.executeUpdate();
        try (ResultSet rs = statement.getGeneratedKeys()) {
          if (rs.next()) {
            entity.setId(rs.getLong(1));
          }
        }
        return entity;
      } catch (SQLException e) {
        throw BlogException.internal("Failed to save: " + entity + ": " + e.getMessage() + "\n" + query);
      }
    });
  }

  public List<T> saveAll(List<T> entities) {
    if (entities == null || entities.isEmpty()) {
      return entities;
    }
    String columns = projectionMetadata.getInsertClause();
    // Using ? placeholders for batch
    String placeholders = Arrays.stream(entities.get(0).getClass().getDeclaredFields())
        .filter(field -> {
          if (field.getName().equals("id"))
            return false;
          Class<?> type = field.getType();
          return !HasId.class.isAssignableFrom(type) && !Collection.class.isAssignableFrom(type);
        })
        .map(f -> "?")
        .collect(Collectors.joining(", "));

    String query = "INSERT INTO " + tableName + columns + " VALUES (" + placeholders + ")";

    return withConnection(conn -> {
      try (PreparedStatement statement = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
        for (T entity : entities) {
          int idx = 1;
          for (Field field : entity.getClass().getDeclaredFields()) {
            if (field.getName().equals("id"))
              continue;
            Class<?> type = field.getType();
            if (HasId.class.isAssignableFrom(type) || Collection.class.isAssignableFrom(type))
              continue;

            field.setAccessible(true);
            Object value = field.get(entity);
            statement.setObject(idx++, value);
          }
          statement.addBatch();
        }
        statement.executeBatch();
        try (ResultSet rs = statement.getGeneratedKeys()) {
          for (T entity : entities) {
            if (rs.next()) {
              entity.setId(rs.getLong(1));
            }
          }
        }
        return entities;
      } catch (SQLException | IllegalAccessException e) {
        throw BlogException.internal("Failed to saveAll entities: " + e.getMessage());
      }
    });
  }

  public T update(T entity) {
    String query = "UPDATE " + tableName + " SET " + getUpdateClause(entity) + " WHERE id = ?";
    safeExecuteQuery(query, null, getValues(entity).toArray(new Object[0]), entity.getId());
    return entity;
  }

  public void deleteById(Long id) {
    String query = "DELETE FROM " + tableName + " WHERE id = ?";
    withConnection(conn -> {
      try (PreparedStatement statement = conn.prepareStatement(query)) {
        statement.setLong(1, id);
        statement.executeUpdate();
        return null;
      } catch (SQLException e) {
        throw BlogException.internal("Failed to delete by id: " + id + ": " + e.getMessage());
      }
    });
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
