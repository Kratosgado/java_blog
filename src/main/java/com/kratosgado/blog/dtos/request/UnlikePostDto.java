
package com.kratosgado.blog.dtos.request;

import com.kratosgado.blog.utils.validators.Numbers.Min;
import com.kratosgado.blog.utils.validators.Objects.NotNull;

public record UnlikePostDto(
    @NotNull @Min(1) int postId,
    @NotNull @Min(1) int userId) {
}
