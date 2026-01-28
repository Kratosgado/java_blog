package com.kratosgado.blog.backend.repositories.jdbc;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
// import java.util.Map; // removed unused import
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.utils.ProjectionMetadata;
import com.kratosgado.blog.interfaces.HasId;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseRepository<T extends HasId> {

  protected Connection connection;
  protected String tableName;
  private ProjectionMetadata projectionMetadata;
  // Holds fieldName => repository for relationships
  private final Map<String, BaseRepository<?>> relationshipRepositories = new HashMap<>();

  public BaseRepository(Connection connection, Class<T> entityClass) {
    this.connection = connection;
    this.projectionMetadata = new ProjectionMetadata(entityClass);
  }

  /**
   * Register a related entity repository for eager relationship loading.
   * 
   * @param fieldName  the field name on the entity
   * @param repository the repository to use to load related entities
   */
  public void registerRelationshipRepository(String fieldName, BaseRepository<?> repository) {
    relationshipRepositories.put(fieldName, repository);
  }

  // Changed: Use toEntityFlat for basic fields; toEntity now loads relationships.
  public abstract T toEntityFlat(ResultSet rs) throws SQLException;

  /**
   * Loads entity and eagerly loads relationships via registered repositories.
   */
  public T toEntity(ResultSet rs) throws SQLException {
    T entity = toEntityFlat(rs);
    loadRelationships(entity, rs);
    return entity;
  }

  /**
   * Uses metadata and registered repositories to eagerly load and set
   * relationships on the entity.
   */
  @SuppressWarnings("unchecked")
  protected void loadRelationships(T entity, ResultSet rs) {
    for (var field : projectionMetadata.getRelationshipFields()) {
      BaseRepository<?> repo = relationshipRepositories.get(field.getName());
      if (repo == null) {
        continue; // Can't load: repository mapping missing
      }
      try {
        // Try loading by convention: fieldName_id or fieldName
        String fkColumn = field.getName() + "_id";
        // Try both snake_case and field name
        Object fkValue;
        try {
          fkValue = rs.getObject(fkColumn);
        } catch (SQLException e) {
          try {
            fkValue = rs.getObject(field.getName());
          } catch (SQLException ex2) {
            continue; // Cannot load without foreign key
          }
        }
        if (fkValue == null)
          continue;
        // ONE-to-ONE or MANY-to-ONE
        Object relEntity = repo.findById(Long.valueOf(fkValue.toString())).orElse(null);
        field.setAccessible(true);
        field.set(entity, relEntity);
      } catch (Exception e) {
        System.err.printf("Failed to load relationship %s for entity %s: %s%n", field.getName(), entity,
            e.getMessage());
      }
    }
  }

  // get get values from entity
  private List<String> getValues(T entity) {
    return Arrays.stream(entity.getClass().getDeclaredFields())
        .filter(field -> !field.getName().equals("id"))
        .map(field -> {
          try {
            field.setAccessible(true);
            var value = field.get(entity);
            return "'" + value.toString() + "'";
          } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
          }
        })
        .collect(Collectors.toList());
  }

  private String getColumnValues(T entity) {
    return String.join(", ", getValues(entity));
  }

  public void safeExecuteQuery(String query, Function<ResultSet, Void> mapper, Object... params) {
    try (PreparedStatement statement = connection.prepareStatement(query)) {
      for (int i = 0; i < params.length; i++) {
        statement.setObject(i + 1, params[i]);
      }
      ResultSet rs = statement.executeQuery();
      if (mapper == null) {
        return;
      }
      while (rs.next()) {
        mapper.apply(rs);
      }
    } catch (SQLException e) {
      throw BlogException.internal("Failed to execute query: " + query + ": " + e.getMessage());
    }
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
      // throw BlogException.internal("Failed to save: " + entity + ": " +
      // e.getMessage());
    }
    return entity;
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
          sb.append(field.getName()).append(" = ?,");
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

  public T update(T entity) {
    String query = "UPDATE " + tableName + " SET " + getUpdateClause(entity) + " WHERE id = ?";
    safeExecuteQuery(query, null, getValues(entity).toArray(new Object[0]), entity.getId());
    return entity;
  }

  public List<T> findAll() {
    List<T> entities = new ArrayList<>();
    String query = "SELECT * FROM " + tableName;
    try (PreparedStatement statement = connection.prepareStatement(query);
        ResultSet rs = statement.executeQuery()) {
      while (rs.next()) {
        entities.add(toEntity(rs));
      }
    } catch (SQLException e) {
      throw BlogException.internal("Failed to find all: " + e.getMessage());
    }
    return entities;
  }

  public List<T> findAll(int limit, int offset) {
    List<T> entities = new ArrayList<>();
    String query = "SELECT * FROM " + tableName + " LIMIT ? OFFSET ?";
    safeExecuteQuery(query, rs -> {
      try {
        while (rs.next()) {
          entities.add(toEntity(rs));
        }
      } catch (SQLException e) {
        throw BlogException.internal("Failed to map entity: " + e.getMessage());
      }
      return null;
    }, limit, offset);

    return entities;
  }

  public List<T> findAllByIds(List<Long> ids) {
    if (ids == null || ids.isEmpty())
      return new ArrayList<>();
    List<T> entities = new ArrayList<>();
    String inClause = ids.stream().map(i -> "?").reduce((a, b) -> a + ", " + b).orElse("?");
    String query = "SELECT * FROM " + tableName + " WHERE id IN (" + inClause + ")";
    try (PreparedStatement statement = connection.prepareStatement(query)) {
      for (int i = 0; i < ids.size(); i++) {
        statement.setObject(i + 1, ids.get(i));
      }
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        entities.add(toEntity(rs));
      }
    } catch (SQLException e) {
      throw BlogException.internal("Failed to load Posts by IDs: " + e.getMessage());
    }
    return entities;
  }

  public Optional<T> findById(Long id) {
    String query = "SELECT * FROM " + tableName + " WHERE id = ?";
    try (PreparedStatement statement = connection.prepareStatement(query)) {
      statement.setLong(1, id);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        return Optional.of(toEntity(rs));
      }
    } catch (SQLException e) {
      throw BlogException.internal("Failed to find by id: " + id + ": " + e.getMessage());
    }
    return Optional.empty();
  }

  public Optional<T> findBySlug(String slug) {
    String query = "SELECT * FROM " + tableName + " WHERE slug = ?";
    try (PreparedStatement statement = connection.prepareStatement(query)) {
      statement.setString(1, slug);
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        return Optional.of(toEntity(rs));

      }
    } catch (SQLException e) {
      throw BlogException.internal("Failed to find by id: " + slug + ": " + e.getMessage());
    }
    return Optional.empty();
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
}
