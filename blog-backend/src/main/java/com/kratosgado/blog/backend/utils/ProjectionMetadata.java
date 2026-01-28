package com.kratosgado.blog.backend.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kratosgado.blog.interfaces.HasId;

public class ProjectionMetadata {

  private final Class<?> projectionType;
  private final List<String> columns;
  private final Map<String, String> columnMappings; // field -> column
  private final List<Field> relationshipFields = new ArrayList<>(); // fields referencing entities

  public ProjectionMetadata(Class<?> projectionType) {
    this.projectionType = projectionType;
    this.columns = new ArrayList<>();
    this.columnMappings = new HashMap<>();
    extractColumns();
  }

  private void extractColumns() {
    if (projectionType.isRecord()) {
      extractFromRecord();
    } else if (projectionType.isInterface()) {
      extractFromInterface();
    } else {
      extractFromClass();
    }
  }

  private void extractFromRecord() {
    RecordComponent[] components = projectionType.getRecordComponents();
    for (RecordComponent component : components) {
      String fieldName = component.getName();
      String columnName = toSnakeCase(fieldName);
      columns.add(columnName);
      columnMappings.put(fieldName, columnName);
    }
  }

  private void extractFromInterface() {
    for (Method method : projectionType.getMethods()) {
      if (method.getName().startsWith("get") &&
          method.getParameterCount() == 0 &&
          !method.getDeclaringClass().equals(Object.class)) {

        String fieldName = uncapitalize(method.getName().substring(3));
        String columnName = toSnakeCase(fieldName);
        columns.add(columnName);
        columnMappings.put(fieldName, columnName);
      }
    }
  }

  private void extractFromClass() {
    for (Field field : projectionType.getDeclaredFields()) {
      if (field.getName().equals("id")) {
        continue;
      }
      if (!Modifier.isStatic(field.getModifiers())) {
        field.setAccessible(true);
        // Detect relationship fields: referencing HasId or List of HasId
        Class<?> type = field.getType();
        if (HasId.class.isAssignableFrom(type)) {
          relationshipFields.add(field);
          continue;
        }

        if (Collection.class.isAssignableFrom(type)) {
          continue;
        }

        String fieldName = field.getName();
        String columnName = toSnakeCase(fieldName);
        columns.add(columnName);
        columnMappings.put(fieldName, columnName);
      }
    }
  }

  public List<Field> getRelationshipFields() {
    return relationshipFields;
  }

  public String getSelectClause() {
    return String.join(", ", columns);
  }

  public String getInsertClause() {
    return String.format("(%s)", String.join(", ", columns));
  }

  public List<String> getColumns() {
    return columns;
  }

  public Map<String, String> getColumnMappings() {
    return columnMappings;
  }

  private String toSnakeCase(String camelCase) {
    return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
  }

  private String uncapitalize(String str) {
    if (str == null || str.isEmpty())
      return str;
    return str.substring(0, 1).toLowerCase() + str.substring(1);
  }
}
