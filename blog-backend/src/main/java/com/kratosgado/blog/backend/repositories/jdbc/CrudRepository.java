package com.kratosgado.blog.backend.repositories.jdbc;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
    List<String> columns = projectionMetadata.getColumns();
    String columnClause = "(" + String.join(", ", columns) + ")";
    String placeholders = columns.stream().map(c -> "?").collect(Collectors.joining(", "));
    String query = "INSERT INTO " + tableName + " " + columnClause + " VALUES (" + placeholders + ")";

    return withConnection(conn -> {
      try (PreparedStatement statement = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
        List<Object> values = getEntityValues(entity);
        for (int i = 0; i < values.size(); i++) {
          statement.setObject(i + 1, values.get(i));
        }
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
    List<String> columns = projectionMetadata.getColumns();
    String columnClause = "(" + String.join(", ", columns) + ")";
    String placeholders = columns.stream().map(c -> "?").collect(Collectors.joining(", "));
    String query = "INSERT INTO " + tableName + " " + columnClause + " VALUES (" + placeholders + ")";

    return withConnection(conn -> {
      try (PreparedStatement statement = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
        for (T entity : entities) {
          List<Object> values = getEntityValues(entity);
          for (int i = 0; i < values.size(); i++) {
            statement.setObject(i + 1, values.get(i));
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
      } catch (SQLException e) {
        throw BlogException.internal("Failed to saveAll entities: " + e.getMessage());
      }
    });
  }

  public T update(T entity) {
    List<Object> values = new ArrayList<>();
    StringBuilder sb = new StringBuilder();

    for (Field field : entity.getClass().getDeclaredFields()) {
      if (field.getName().equals("id"))
        continue;
      if (Modifier.isStatic(field.getModifiers()))
        continue;

      Class<?> type = field.getType();
      if (HasId.class.isAssignableFrom(type) || Collection.class.isAssignableFrom(type))
        continue;

      try {
        field.setAccessible(true);
        Object value = field.get(entity);
        if (value != null) {
          String columnName = toSnakeCase(field.getName());
          sb.append(columnName).append(" = ?, ");
          values.add(prepareParameter(value));
        }
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }

    if (sb.length() > 0) {
      sb.setLength(sb.length() - 2);
    } else {
      return entity;
    }

    String query = "UPDATE " + tableName + " SET " + sb.toString() + " WHERE id = ?";
    values.add(entity.getId());

    safeExecuteQuery(query, null, values.toArray());
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

  private List<Object> getEntityValues(T entity) {
    List<Object> values = new ArrayList<>();
    for (Field field : entity.getClass().getDeclaredFields()) {
      if (field.getName().equals("id"))
        continue;
      if (Modifier.isStatic(field.getModifiers()))
        continue;

      Class<?> type = field.getType();
      if (HasId.class.isAssignableFrom(type) || Collection.class.isAssignableFrom(type))
        continue;

      try {
        field.setAccessible(true);
        values.add(prepareParameter(field.get(entity)));
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
    return values;
  }

  private String toSnakeCase(String camelCase) {
    return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
  }
}
