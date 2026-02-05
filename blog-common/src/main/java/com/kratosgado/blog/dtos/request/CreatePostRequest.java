package com.kratosgado.blog.dtos.request;

import com.kratosgado.blog.enums.PostStatus;
import com.kratosgado.blog.validation.ValidEnum;
import com.kratosgado.blog.validation.ValidLongArray;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new blog post.
 * Contains all necessary information to create a post including title, content,
 * category assignment, and optional metadata.
 */
@Schema(description = "Request payload for creating a new blog post")
public record CreatePostRequest(
    @Schema(description = "Post title", example = "10 Tips for Better Code", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 3, maxLength = 200)
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    String title,

    @Schema(description = "Full post content in markdown or HTML format", example = "This is the full content of the blog post...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Content is required")
    String content,

    @Schema(description = "Brief excerpt or summary of the post", example = "A quick guide to writing better code")
    String excerpt,

    @Schema(description = "ID of the category this post belongs to", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Category is required")
    Long categoryId,

    @Schema(description = "URL or path to the cover image", example = "https://example.com/images/cover.jpg")
    String coverImage,

    @Schema(description = "Publication status of the post", example = "draft", allowableValues = {"draft", "published"})
    @ValidEnum(enumClass = PostStatus.class, message = "Status must be either 'draft' or 'published'")
    String status,

    @Schema(description = "Array of tag IDs to associate with this post", example = "[1, 2, 3]")
    @ValidLongArray(min = 1, message = "Invalid tag IDs")
    Long[] tagIds
) {
}
