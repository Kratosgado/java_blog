package com.kratosgado.blog.dtos.response;

import com.kratosgado.blog.enums.PostStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Projection interfaces for post response data. Uses composition pattern to create different views
 * of post data with varying levels of detail. Spring Data JPA uses these interfaces for efficient
 * query projections, preventing N+1 problems. Different combinations are used based on API endpoint
 * requirements.
 */
@Schema(description = "Collection of post projection interfaces for different response views")
public interface PostResponse {

  /**
   * Base interface for post summary information. Contains essential post data without relationships
   * or full content.
   */
  @Schema(description = "Basic post summary without relationships")
  public interface PostSummary {
    @Schema(description = "Unique post identifier", example = "1")
    Long getId();

    @Schema(description = "Post title", example = "Getting Started with Spring Boot")
    String getTitle();

    @Schema(description = "URL-friendly post slug", example = "getting-started-with-spring-boot")
    String getSlug();

    @Schema(
        description = "Brief excerpt or summary of the post",
        example = "Learn the basics of Spring Boot...")
    String getExcerpt();

    @Schema(
        description = "Publication status of the post",
        example = "PUBLISHED",
        allowableValues = {"DRAFT", "PUBLISHED", "ARCHIVED"})
    PostStatus getStatus();

    @Schema(
        description = "URL to the post's cover image",
        example = "https://example.com/images/spring-boot.jpg")
    String getCoverImage();

    @Schema(description = "Post creation timestamp", example = "2026-02-05T10:30:00")
    LocalDateTime getCreatedAt();

    @Schema(description = "Number of times the post has been viewed", example = "543")
    Integer getViews();

    @Schema(description = "Number of likes received", example = "87")
    Integer getLikesCount();
  }

  /**
   * Extended post interface including full content. Adds complete post body and update timestamp to
   * PostSummary.
   */
  @Schema(description = "Post data including full content and update timestamp")
  public interface IPost extends PostSummary {
    @Schema(
        description = "Full post content in markdown or HTML format",
        example = "# Introduction\n\nSpring Boot makes it easy to create...")
    String getContent();

    @Schema(description = "Last update timestamp", example = "2026-02-05T14:20:00")
    LocalDateTime getUpdatedAt();
  }

  /** Mixin interface for including author/user information. */
  @Schema(description = "Mixin interface providing author details")
  public interface WithUser {
    @Schema(description = "Author information")
    AuthorSummary getUser();
  }

  /** Mixin interface for including category information. */
  @Schema(description = "Mixin interface providing category details")
  public interface WithCategory {
    @Schema(description = "Category information")
    CategorySummary getCategory();
  }

  /** Mixin interface for including tag list. */
  @Schema(description = "Mixin interface providing list of tags")
  public interface WithTag {
    @Schema(description = "List of tags associated with the post")
    List<TagSummary> getTags();
  }

  /**
   * Complete post summary view with all relationships. Used in post listing endpoints for
   * comprehensive overview without full content.
   */
  @Schema(description = "Complete post summary with author, category, and tags")
  public interface PostView extends PostSummary, WithUser, WithCategory, WithTag {}

  /**
   * Complete post details view with full content and all relationships. Used in post detail
   * endpoints for complete post information.
   */
  @Schema(description = "Complete post details with full content, author, category, and tags")
  public interface PostDetails extends IPost, WithUser, WithCategory, WithTag {}

  /**
   * Post view without user information. Used when author details are not needed or should be
   * excluded.
   */
  @Schema(description = "Post summary with category and tags but without author information")
  public interface PostWithoutUser extends PostSummary, WithCategory, WithTag {}

  /**
   * Post view without tag information. Used when tag relationships should be excluded for
   * performance.
   */
  @Schema(description = "Post summary with author and category but without tags")
  public interface PostWithoutTag extends PostSummary, WithUser, WithCategory {}

  /**
   * Post view without category information. Used when category relationships should be excluded.
   */
  @Schema(description = "Post summary with author and tags but without category")
  public interface PostWithoutCategory extends PostSummary, WithUser, WithTag {}
}
