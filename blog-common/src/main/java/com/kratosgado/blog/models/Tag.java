package com.kratosgado.blog.models;

import java.util.List;

import com.kratosgado.blog.interfaces.HasId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tags", indexes = {
    @Index(name = "idx_tags_slug", columnList = "slug"),
})
public class Tag implements HasId {
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
  private List<Post> posts;

  private transient Long postCount = 0L;

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
