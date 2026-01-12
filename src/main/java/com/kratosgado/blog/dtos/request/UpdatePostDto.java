
package com.kratosgado.blog.dtos.request;

import com.kratosgado.blog.utils.validators.Numbers.Min;
import com.kratosgado.blog.utils.validators.Objects.NotNull;
import com.kratosgado.blog.utils.validators.Strings.IsString;

public record UpdatePostDto(
    @NotNull @Min(1) int id,
    @IsString(maxLenth = 50, minLenth = 4) String title,
    String content,
    @IsString(maxLenth = 100) String excerpt,
    String status,
    String coverImage) {
}
