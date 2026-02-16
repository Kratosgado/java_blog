package com.kratosgado.blog.models;

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
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "posts",
    indexes = {
      @Index(name = "idx_posts_slug", columnList = "slug"),
    })
@NamedEntityGraph(
    name = "post-with-details",
    attributeNodes = {
      @NamedAttributeNode("user"),
      @NamedAttributeNode("category"),
      @NamedAttributeNode("tags")
    })
@NamedEntityGraph(
    name = "post-without-user",
    attributeNodes = {@NamedAttributeNode("tags"), @NamedAttributeNode("category")})
@NamedEntityGraph(
    name = "post-without-category",
    attributeNodes = {@NamedAttributeNode("user"), @NamedAttributeNode("tags")})
@NamedEntityGraph(
    name = "post-without-tags",
    attributeNodes = {@NamedAttributeNode("user"), @NamedAttributeNode("category")})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
  @JoinTable(
      name = "post_tags",
      joinColumns = @JoinColumn(name = "post_id"),
      inverseJoinColumns = @JoinColumn(name = "tag_id"))
  private List<Tag> tags;
}
