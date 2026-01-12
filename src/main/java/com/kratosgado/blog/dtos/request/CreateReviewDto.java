
package com.kratosgado.blog.dtos.request;

import com.kratosgado.blog.utils.validators.Numbers.Min;
import com.kratosgado.blog.utils.validators.Objects.NotNull;
import com.kratosgado.blog.utils.validators.Strings.IsString;

public record CreateReviewDto(
    @NotNull @Min(1) int postId,
    @NotNull @Min(1) int userId,
    @NotNull @Min(1) int rating, // 1-5 stars
    @IsString(minLenth = 1, maxLenth = 255) String title,
    @IsString(minLenth = 1, maxLenth = 5000) String content) {
}
