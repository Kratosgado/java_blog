package com.kratosgado.blog.models;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "categories")
public class Category {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(nullable = false, unique = true)
  private String name;
  
  @Column(nullable = false, unique = true)
  private String slug;
  
  @Column(columnDefinition = "TEXT")
  private String description;
  
  @Column(name = "created_at")
  private LocalDateTime createdAt;
  
  @Transient
  private Integer postCount;
  
  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }
}
