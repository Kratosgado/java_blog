package com.kratosgado.blog.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "tags",
    indexes = {
      @Index(name = "idx_tags_slug", columnList = "slug"),
    })
public class Tag {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String name;

  @Column(unique = true, nullable = false)
  private String slug;

  @Column(columnDefinition = "TEXT")
  private String description;

  @ManyToMany(mappedBy = "tags")
  @JsonIgnore
  private List<Post> posts;

  public Tag(Long id, String name, String slug, String description) {
    this.id = id;
    this.name = name;
    this.slug = slug;
    this.description = description;
  }

  public Tag(String name, String slug, String description) {
    this.name = name;
    this.slug = slug;
    this.description = description;
  }
}
