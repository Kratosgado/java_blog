package com.kratosgado.blog.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Tag {
  private Integer id;
  private String name;
  private String slug;
  private String description;
  private Integer postCount = 0;

  public Tag(String name, String slug, String description) {
    this.name = name;
    this.slug = slug;
    this.description = description;
  }
}
