
package com.kratosgado.blog.dtos.request;

import com.kratosgado.blog.utils.validators.Objects.NotEmpty;
import com.kratosgado.blog.utils.validators.Objects.NotNull;
import com.kratosgado.blog.utils.validators.Strings.IsString;

public record CreatePostDto(
    @NotNull int userId,
    @IsString(maxLenth = 50, minLenth = 4) String title,
    @NotEmpty String content,
    @IsString(maxLenth = 100) String excerpt,
    @NotNull String status,
    String featuredImage) {
}
