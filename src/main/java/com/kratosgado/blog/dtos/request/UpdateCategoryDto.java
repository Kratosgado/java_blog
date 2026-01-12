
package com.kratosgado.blog.dtos.request;

import com.kratosgado.blog.utils.validators.Numbers.Min;
import com.kratosgado.blog.utils.validators.Objects.NotNull;
import com.kratosgado.blog.utils.validators.Strings.IsString;

public record UpdateCategoryDto(
    @NotNull @Min(1) int id,
    @NotNull @IsString(minLenth = 2, maxLenth = 50) String name,
    @NotNull @IsString(minLenth = 5, maxLenth = 255) String description) {
}
