package com.kratosgado.blog.models;

import java.time.LocalDateTime;
import java.util.List;

import com.kratosgado.blog.enums.PostStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "posts", indexes = {
    @Index(name = "idx_posts_slug", columnList = "slug"),
    @Index(name = "idx_posts_title", columnList = "title"),
    @Index(name = "idx_posts_status", columnList = "status"),
    @Index(name = "idx_posts_created_at", columnList = "created_at"),
    @Index(name = "idx_posts_user_id", columnList = "user_id"),
    @Index(name = "idx_posts_category_id", columnList = "category_id")
})
@NamedEntityGraph(name = "post-with-details", attributeNodes = {
    @NamedAttributeNode("user"),
    @NamedAttributeNode("category"),
    @NamedAttributeNode("tags")
})

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Post {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(unique = true, nullable = false)
  private String slug;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String content;

  @Column(columnDefinition = "TEXT")
  private String excerpt;

  @Enumerated(EnumType.STRING)
  private PostStatus status;

  @Column(name = "created_at", updatable = false)
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
    if (status == null) {
      status = PostStatus.draft;
    }
  }

  @PreUpdate
  public void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "user_id", insertable = false, updatable = false)
  private Long userId;

  @ManyToOne
  @JoinColumn(name = "category_id")
  private Category category;

  @ManyToMany
  @JoinTable(name = "post_tags", joinColumns = @JoinColumn(name = "post_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
  private List<Tag> tags;

  public Long getUserId() {
    return user != null ? user.getId() : null;
  }

  public void setUserId(Long userId) {
    if (this.user == null) {
      this.user = new User();
    }
    this.user.setId(userId);
  }

  public Long getCategoryId() {
    return category != null ? category.getId() : null;
  }

  public void setCategoryId(Long categoryId) {
    if (this.category == null) {
      this.category = new Category();
    }
    this.category.setId(categoryId);
  }
}
