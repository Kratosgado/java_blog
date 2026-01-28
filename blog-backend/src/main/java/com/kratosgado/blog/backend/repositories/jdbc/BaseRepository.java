package com.kratosgado.blog.backend.repositories.jdbc;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
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
  protected ProjectionMetadata projectionMetadata;
  // Holds fieldName => repository for relationships
  private final Map<String, BaseRepository<?>> relationshipRepositories = new HashMap<>();
  
  protected static class ManyToManyConfig {
    String fieldName;
    BaseRepository<?> targetRepo;
    String junctionTable;
    String joinColumn;
    String inverseJoinColumn;

    public ManyToManyConfig(String fieldName, BaseRepository<?> targetRepo, String junctionTable, String joinColumn, String inverseJoinColumn) {
        this.fieldName = fieldName;
        this.targetRepo = targetRepo;
        this.junctionTable = junctionTable;
        this.joinColumn = joinColumn;
        this.inverseJoinColumn = inverseJoinColumn;
    }
  }

  private final Map<String, ManyToManyConfig> manyToManyRelationships = new HashMap<>();

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

  public void registerManyToManyRelationship(String fieldName, BaseRepository<?> targetRepo, String junctionTable, String joinColumn, String inverseJoinColumn) {
      manyToManyRelationships.put(fieldName, new ManyToManyConfig(fieldName, targetRepo, junctionTable, joinColumn, inverseJoinColumn));
  }

  public ProjectionMetadata getProjectionMetadata() {
    return projectionMetadata;
  }

  public String getTableName() {
    return tableName;
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
      String fieldName = field.getName();
      BaseRepository<?> repo = relationshipRepositories.get(fieldName);
      if (repo == null) {
        continue; // Can't load: repository mapping missing
      }

      // 1. Try to load from JOINed columns (if present in RS)
      String prefix = fieldName + "_rel_";
      boolean joinedDataFound = false;
      try {
        // Check if the ID column for the related entity is present and not null
        Object joinedId = null;
        try {
            joinedId = rs.getObject(prefix + "id");
        } catch(SQLException ignored) {
        }

        if (joinedId != null) {
          // Create a proxy ResultSet that maps calls to the prefixed columns
          ResultSet proxyRs = createResultSetProxy(rs, prefix);
          Object relEntity = repo.toEntityFlat(proxyRs);
          
          field.setAccessible(true);
          field.set(entity, relEntity);
          joinedDataFound = true;
        }
      } catch (Exception e) {
          // Log if needed, but safe to continue to fallback
      }

      if (joinedDataFound) continue;

      // 2. Fallback: Lazy load (N+1) if join didn't happen or data was null
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

  private void loadManyToManyRelationships(T entity, ResultSet rs) {
      for (ManyToManyConfig config : manyToManyRelationships.values()) {
          String prefix = config.fieldName + "_rel_";
          try {
              Object joinedId = null;
              try {
                  joinedId = rs.getObject(prefix + "id");
              } catch (SQLException ignored) {
              }

              if (joinedId != null) {
                  ResultSet proxyRs = createResultSetProxy(rs, prefix);
                  Object relEntity = config.targetRepo.toEntityFlat(proxyRs);
                  
                  // Add to collection
                  Field field = entity.getClass().getDeclaredField(config.fieldName);
                  field.setAccessible(true);
                  Collection<Object> collection = (Collection<Object>) field.get(entity);
                  if (collection == null) {
                      collection = new ArrayList<>();
                      field.set(entity, collection);
                  }
                  
                  boolean exists = false;
                  if (relEntity instanceof HasId) {
                      Long relId = ((HasId)relEntity).getId();
                      for (Object existing : collection) {
                          if (existing instanceof HasId && ((HasId)existing).getId().equals(relId)) {
                              exists = true; 
                              break;
                          }
                      }
                  }
                  if (!exists) {
                      collection.add(relEntity);
                  }
              }
          } catch (Exception e) {
               // ignore
          }
      }
  }

  private ResultSet createResultSetProxy(ResultSet original, String prefix) {
    return (ResultSet) Proxy.newProxyInstance(
        ResultSet.class.getClassLoader(),
        new Class[] { ResultSet.class },
        new InvocationHandler() {
          @Override
          public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
             if (args != null && args.length > 0 && args[0] instanceof String) {
               String label = (String) args[0];
               if (method.getName().startsWith("get") || method.getName().equals("findColumn") || method.getName().equals("getObject")) {
                  Object[] newArgs = Arrays.copyOf(args, args.length);
                  newArgs[0] = prefix + label;
                  try {
                      return method.invoke(original, newArgs);
                  } catch (InvocationTargetException e) {
                      throw e.getCause();
                  }
               }
             }
             return method.invoke(original, args);
          }
        });
  }

  // Generate SELECT query with JOINs
  protected String generateSelectQuery(String whereClause) {
    StringBuilder select = new StringBuilder("SELECT ");

    // Main columns (explicitly select id + fields)
    select.append("t.id");
    List<String> columns = projectionMetadata.getColumns();
    if (!columns.isEmpty()) {
      select.append(", ");
      select.append(columns.stream().map(c -> "t." + c).collect(Collectors.joining(", ")));
    }

    // Relationship columns
    for (var entry : relationshipRepositories.entrySet()) {
      String fieldName = entry.getKey();
      BaseRepository<?> repo = entry.getValue();
      String prefix = fieldName + "_rel_";
      String tableAlias = fieldName + "_tbl";

      // Related ID
      select.append(", ").append(tableAlias).append(".id AS ").append(prefix).append("id");

      // Related fields
      for (String col : repo.getProjectionMetadata().getColumns()) {
        select.append(", ").append(tableAlias).append(".").append(col).append(" AS ").append(prefix).append(col);
      }
    }
    
    // Many-to-Many columns
    for (var entry : manyToManyRelationships.entrySet()) {
        String fieldName = entry.getKey();
        ManyToManyConfig config = entry.getValue();
        BaseRepository<?> repo = config.targetRepo;
        String prefix = fieldName + "_rel_";
        String tableAlias = fieldName + "_tbl";
        
        select.append(", ").append(tableAlias).append(".id AS ").append(prefix).append("id");
        for (String col : repo.getProjectionMetadata().getColumns()) {
            select.append(", ").append(tableAlias).append(".").append(col).append(" AS ").append(prefix).append(col);
        }
    }

    select.append(" FROM ").append(tableName).append(" t");

    // JOINS (One-to-One / Many-to-One)
    for (var entry : relationshipRepositories.entrySet()) {
      String fieldName = entry.getKey();
      BaseRepository<?> repo = entry.getValue();
      String tableAlias = fieldName + "_tbl";
      String fkColumn = fieldName + "_id"; // Convention

      select.append(" LEFT JOIN ").append(repo.getTableName()).append(" ").append(tableAlias)
          .append(" ON t.").append(fkColumn).append(" = ").append(tableAlias).append(".id");
    }
    
    // JOINS (Many-to-Many)
    for (var entry : manyToManyRelationships.entrySet()) {
        String fieldName = entry.getKey();
        ManyToManyConfig config = entry.getValue();
        String junctionAlias = fieldName + "_junction";
        String targetAlias = fieldName + "_tbl";
        
        select.append(" LEFT JOIN ").append(config.junctionTable).append(" ").append(junctionAlias)
              .append(" ON t.id = ").append(junctionAlias).append(".").append(config.joinColumn);
              
        select.append(" LEFT JOIN ").append(config.targetRepo.getTableName()).append(" ").append(targetAlias)
              .append(" ON ").append(junctionAlias).append(".").append(config.inverseJoinColumn).append(" = ").append(targetAlias).append(".id");
    }

    if (whereClause != null && !whereClause.isBlank()) {
      select.append(" ").append(whereClause);
    }

    return select.toString();
  }
  
  protected List<T> executePagedSelect(String whereClause, String orderBy, int limit, int offset, Object... whereParams) {
      // 1. Fetch IDs with pagination
      String idQuery = "SELECT t.id FROM " + tableName + " t " + 
                       (whereClause != null ? " " + whereClause : "") + 
                       (orderBy != null ? " " + orderBy : "") + 
                       " LIMIT ? OFFSET ?";
      
      List<Long> ids = new ArrayList<>();
      
      // Combine whereParams with limit and offset
      Object[] idParams = new Object[whereParams.length + 2];
      System.arraycopy(whereParams, 0, idParams, 0, whereParams.length);
      idParams[whereParams.length] = limit;
      idParams[whereParams.length + 1] = offset;
      
      safeExecuteQuery(idQuery, rs -> {
          try {
             ids.add(rs.getLong(1));
          } catch (SQLException e) {
             throw new RuntimeException(e);
          }
          return null;
      }, idParams);
      
      if (ids.isEmpty()) {
          return new ArrayList<>();
      }
      
      // 2. Fetch full entities by IDs
      String inClause = ids.stream().map(id -> "?").collect(Collectors.joining(", "));
      String fullQuery = generateSelectQuery("WHERE t.id IN (" + inClause + ")" + (orderBy != null ? " " + orderBy : ""));
      
      return executeSelect(fullQuery, ids.toArray());
  }
  
  protected List<T> executeSelect(String query, Object... params) {
      try (PreparedStatement statement = connection.prepareStatement(query)) {
          for (int i = 0; i < params.length; i++) {
              statement.setObject(i + 1, params[i]);
          }
          try (ResultSet rs = statement.executeQuery()) {
              return mapResultSet(rs);
          }
      } catch (SQLException e) {
          throw BlogException.internal("Failed to execute select: " + query + ": " + e.getMessage());
      }
  }
  
  private List<T> mapResultSet(ResultSet rs) throws SQLException {
      Map<Long, T> map = new LinkedHashMap<>();
      while (rs.next()) {
          Long id = rs.getLong("id"); // Assumes first column is t.id (as per generateSelectQuery)
          T entity = map.get(id);
          if (entity == null) {
              entity = toEntityFlat(rs);
              loadRelationships(entity, rs);
              // Init M-to-M collections
              for (String fieldName : manyToManyRelationships.keySet()) {
                  try {
                      Field field = entity.getClass().getDeclaredField(fieldName);
                      field.setAccessible(true);
                      if (field.get(entity) == null) {
                          field.set(entity, new ArrayList<>()); // Default to ArrayList
                      }
                  } catch (Exception e) {
                      // ignore
                  }
              }
              map.put(id, entity);
          }
          loadManyToManyRelationships(entity, rs);
      }
      return new ArrayList<>(map.values());
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
    String query = generateSelectQuery("");
    return executeSelect(query);
  }

  public List<T> findAll(int limit, int offset) {
    List<T> entities = new ArrayList<>();
    String query = generateSelectQuery("LIMIT ? OFFSET ?");
    return executeSelect(query, limit, offset);
  }

  public List<T> findAllByIds(List<Long> ids) {
    if (ids == null || ids.isEmpty())
      return new ArrayList<>();
    List<T> entities = new ArrayList<>();
    String inClause = ids.stream().map(i -> "?").reduce((a, b) -> a + ", " + b).orElse("?");
    String query = generateSelectQuery("WHERE t.id IN (" + inClause + ")");
    return executeSelect(query, ids.toArray());
  }

  public Optional<T> findById(Long id) {
    String query = generateSelectQuery("WHERE t.id = ?");
    List<T> results = executeSelect(query, id);
    return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
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