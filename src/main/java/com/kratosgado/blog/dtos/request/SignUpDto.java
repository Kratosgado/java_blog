package com.kratosgado.blog.dtos.request;

import com.kratosgado.blog.utils.validators.Objects.NotEmpty;
import com.kratosgado.blog.utils.validators.Strings.IsEmail;
import com.kratosgado.blog.utils.validators.Strings.IsString;
import com.kratosgado.blog.utils.validators.Strings.IsStrongPassword;

public record SignUpDto(
    @IsString(minLenth = 4, maxLenth = 20) String username,
    @IsEmail String email,
    @IsStrongPassword String password,
    @NotEmpty String confirmPassword) {
}
