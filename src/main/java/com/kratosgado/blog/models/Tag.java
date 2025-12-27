package com.kratosgado.blog.models;

import java.time.LocalDateTime;

import com.kratosgado.blog.dtos.request.CreateTagDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tag {
  private int id;
  private String name;
  private String slug;
  private String description;
  private LocalDateTime createdAt;
  private int postCount;

  public Tag(String name, String slug, String description) {
    this.name = name;
    this.slug = slug;
    this.description = description;
  }

  public Tag(CreateTagDto dto) {
    this.name = dto.name();
    this.slug = dto.name().toLowerCase().replaceAll("\\s+", "-");
    this.description = dto.description();
  }
}
