package com.kratosgado.blog.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

  @Override
  public boolean isValid(String password, ConstraintValidatorContext context) {
    if (password == null) {
      return false; // Password is required
    }
    if (password.length() < 8)
      return false;
    if (!password.matches(".*\\d.*"))
      return false;
    if (!password.matches(".*[a-z].*"))
      return false;
    if (!password.matches(".*[A-Z].*"))
      return false;
    if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*"))
      return false;
    return true;
  }
}
