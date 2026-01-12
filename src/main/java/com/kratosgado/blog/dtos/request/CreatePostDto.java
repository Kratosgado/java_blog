
package com.kratosgado.blog.dtos.request;

import java.util.List;

import com.kratosgado.blog.utils.validators.Numbers.Min;
import com.kratosgado.blog.utils.validators.Objects.NotEmpty;
import com.kratosgado.blog.utils.validators.Objects.NotNull;
import com.kratosgado.blog.utils.validators.Strings.IsString;

public record CreatePostDto(
    @NotNull int userId,
    @NotNull @Min(value = 1) Integer categoryId,  // Category is mandatory
    @NotNull @NotEmpty List<Integer> tagIds,  // At least one tag is mandatory
    @IsString(maxLenth = 50, minLenth = 4) String title,
    @NotEmpty String content,
    @IsString(maxLenth = 100) String excerpt,
    @NotNull String status,
    String coverImage) {
}
