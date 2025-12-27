
package com.kratosgado.blog.dtos.request;

import com.kratosgado.blog.utils.validators.Objects.NotNull;
import com.kratosgado.blog.utils.validators.Strings.IsString;

public record CreateTagDto(
    @NotNull String name,
    @IsString(minLenth = 1, maxLenth = 255) String description) {
}
