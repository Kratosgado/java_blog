package com.kratosgado.blog.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
  private Integer id;
  private String name;
  private String slug;
  private String description;
}
