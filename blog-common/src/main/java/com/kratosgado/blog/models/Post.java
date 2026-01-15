package com.kratosgado.blog.models;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "posts")
public class Post {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(name = "user_id", nullable = false)
  private Long userId;
  
  @Column(name = "category_id")
  private Long categoryId;
  
  @Column(nullable = false)
  private String title;
  
  @Column(columnDefinition = "TEXT")
  private String content;
  
  @Column(columnDefinition = "TEXT")
  private String excerpt;
  
  @Column(nullable = false)
  private String status;
  
  @Column(name = "created_at")
  private LocalDateTime createdAt;
  
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
  
  private Integer views = 0;
  
  @Column(name = "likes_count")
  private Integer likesCount = 0;
  
  @Column(name = "cover_image")
  private String coverImage;
  
  @Transient
  private String authorName;
  
  @Transient
  private String authorAvatarUrl;
  
  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }
  
  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
