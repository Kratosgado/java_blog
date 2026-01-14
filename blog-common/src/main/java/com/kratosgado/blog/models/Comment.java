package com.kratosgado.blog.models;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "comments")
public class Comment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(name = "post_id", nullable = false)
  private Long postId;
  
  @Column(name = "user_id", nullable = false)
  private Long userId;
  
  @Column(columnDefinition = "TEXT", nullable = false)
  private String content;
  
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CommentStatus status;
  
  @Column(name = "created_at")
  private LocalDateTime createdAt;
  
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
  
  @Transient
  private String authorName;
  
  @Transient
  private String authorAvatarUrl;

  public Comment(Long postId, Long userId, String content) {
    this.postId = postId;
    this.userId = userId;
    this.content = content;
    this.status = CommentStatus.PENDING;
  }
  
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
