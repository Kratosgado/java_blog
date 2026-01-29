package com.kratosgado.blog.models;

import com.kratosgado.blog.interfaces.HasId;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category implements HasId {
  private Long id;

  private String name;

  private String slug;

  private String description;
}
