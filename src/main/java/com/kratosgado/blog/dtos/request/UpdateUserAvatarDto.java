package com.kratosgado.blog.dtos.request;

import com.kratosgado.blog.utils.validators.Numbers.Min;
import com.kratosgado.blog.utils.validators.Objects.NotNull;
import com.kratosgado.blog.utils.validators.Strings.IsString;

public record UpdateUserAvatarDto(
  @NotNull
  @Min(value = 1)
  Integer userId,

  @NotNull
  @IsString(minLenth = 1, maxLenth = 500)
  String avatarUrl
) {
}
