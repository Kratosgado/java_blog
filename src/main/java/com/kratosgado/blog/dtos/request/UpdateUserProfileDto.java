package com.kratosgado.blog.dtos.request;

import com.kratosgado.blog.utils.validators.Numbers.Min;
import com.kratosgado.blog.utils.validators.Objects.NotNull;
import com.kratosgado.blog.utils.validators.Strings.IsString;

public record UpdateUserProfileDto(
  @NotNull
  @Min(value = 1)
  Integer userId,

  @IsString(minLenth = 0, maxLenth = 500)
  String bio,

  @IsString(minLenth = 0, maxLenth = 200)
  String website,

  @IsString(minLenth = 0, maxLenth = 100)
  String location
) {
}
