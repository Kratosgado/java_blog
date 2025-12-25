
package com.kratosgado.blog.dtos.request;

import com.kratosgado.blog.utils.validators.Validator;
import com.kratosgado.blog.utils.validators.Objects.NotEmpty;
import com.kratosgado.blog.utils.validators.Strings.IsString;
import com.kratosgado.blog.utils.validators.Strings.IsStrongPassword;

public record ChangePasswordDto(int id, @NotEmpty() String oldPassword, @IsStrongPassword String newPassword,
    @IsString String confirmNewPassword) {
  public ChangePasswordDto {
    Validator.validate(this);
  }
}
