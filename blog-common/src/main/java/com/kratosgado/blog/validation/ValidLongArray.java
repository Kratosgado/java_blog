package com.kratosgado.blog.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = LongArrayValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidLongArray {
    String message() default "Invalid array values";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
    
    long min() default Long.MIN_VALUE;

    long max() default Long.MAX_VALUE;
    
    boolean unique() default true;
    
    boolean ignoreNulls() default true;
}
