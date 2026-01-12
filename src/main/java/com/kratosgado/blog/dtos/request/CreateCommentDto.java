
package com.kratosgado.blog.dtos.request;

import com.kratosgado.blog.utils.validators.Numbers.Min;
import com.kratosgado.blog.utils.validators.Objects.NotNull;
import com.kratosgado.blog.utils.validators.Strings.IsString;

public record CreateCommentDto(
    @NotNull @Min(1) int postId,
    @NotNull @Min(1) int userId,
    @NotNull String authorName,
    @NotNull String authorAvatarUrl,
    @NotNull @IsString(minLenth = 1, maxLenth = 5000) String content) {
}
