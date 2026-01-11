
package com.kratosgado.blog.models;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

  private int id;
  private String name;
  private String slug;
  private String description;
  private Timestamp createdAt;
}
