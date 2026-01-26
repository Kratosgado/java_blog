package com.kratosgado.blog.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kratosgado.blog.enums.PostStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@NamedEntityGraph(name = "Post.withDetails", attributeNodes = {
    @NamedAttributeNode("user"),
    @NamedAttributeNode("category"),
    @NamedAttributeNode("tags")
})
@Table(name = "posts", indexes = {
    @Index(name = "idx_post_slug", columnList = "slug"),
    @Index(name = "idx_post_user", columnList = "user_id"),
    @Index(name = "idx_post_category", columnList = "category_id"),
    @Index(name = "idx_post_status", columnList = "status"),
    @Index(name = "idx_post_created_at", columnList = "created_at")
})
public class Post {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false, unique = true)
  private String slug;

  @Column(columnDefinition = "TEXT")
  private String content;

  @Column(columnDefinition = "TEXT")
  private String excerpt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private PostStatus status;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  private Integer views = 0;

  @Column(name = "likes_count")
  private Integer likesCount = 0;

  @Column(name = "cover_image")
  private String coverImage;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
    status = PostStatus.draft;
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  // relationships
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false)
  @JsonIgnoreProperties(value = { "hibernateLazyInitializer", "handler", "bio", "location", "website",
      "createdAt", "role" })
  private User user;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id", insertable = false, updatable = false)
  @JsonIgnoreProperties(value = { "hibernateLazyInitializer", "handler", "postCount", "description", "createdAt" })
  private Category category;

  @Column(name = "category_id")
  private Long categoryId;

  @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
  @JoinTable(name = "post_tags", joinColumns = @JoinColumn(name = "post_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
  @JsonIgnoreProperties(value = { "hibernateLazyInitializer", "handler", "createdAt", "description", "postCount" })
  private List<Tag> tags = new ArrayList<>();
}
