
package com.kratosgado.blog.dtos.request;

import com.kratosgado.blog.utils.validators.Strings.IsEmail;
import com.kratosgado.blog.utils.validators.Strings.IsString;
import com.kratosgado.blog.utils.validators.Strings.IsStrongPassword;

public record LoginDto(@IsEmail String email, @IsString String password) {
}
