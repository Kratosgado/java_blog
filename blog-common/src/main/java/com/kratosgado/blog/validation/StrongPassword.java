package com.kratosgado.blog.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@NotBlank
public @interface StrongPassword {

  String message() default "Password must be at least 8 characters long, contain at least one digit, one lowercase letter, one uppercase letter, and one special character";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
