package com.kratosgado.blog.dtos.request;

import com.kratosgado.blog.enums.PostStatus;
import com.kratosgado.blog.validation.ValidLongArray;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating an existing blog post.
 * All fields are optional - only provided fields will be updated.
 * Allows partial updates to post title, content, category, status, and
 * associated tags.
 */
@Schema(description = "Request payload for updating an existing blog post. All fields are optional.")
public record UpdatePostRequest(
    @Schema(description = "Updated post title", example = "Updated: 10 Tips for Better Code", minLength = 3, maxLength = 200) @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters") String title,

    @Schema(description = "Updated full post content in markdown or HTML format", example = "This is the updated content of the blog post...") String content,

    @Schema(description = "Updated brief excerpt or summary", example = "An updated guide to writing better code") String excerpt,

    @Schema(description = "Updated category ID for the post", example = "2") Long categoryId,

    @Schema(description = "Updated URL or path to the cover image", example = "https://example.com/images/new-cover.jpg") String coverImage,

    @Schema(description = "Updated publication status", example = "published", allowableValues = {
        "draft", "published", "archived" }) PostStatus status,

    @Schema(description = "Updated array of tag IDs to associate with this post", example = "[1, 3, 5]") @ValidLongArray(min = 1, message = "Invalid tag IDs") Long[] tagIds) {
}
