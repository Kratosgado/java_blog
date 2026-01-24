package com.kratosgado.blog.validation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class LongArrayValidator implements ConstraintValidator<ValidLongArray, Long[]> {

    private long min;
    private long max;
    private boolean unique;
    private boolean ignoreNulls;

    @Override
    public void initialize(ValidLongArray constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
        this.unique = constraintAnnotation.unique();
        this.ignoreNulls = constraintAnnotation.ignoreNulls();
    }

    @Override
    public boolean isValid(Long[] values, ConstraintValidatorContext context) {
        if (values == null) {
            return true;
        }

        if (ignoreNulls) {
            // Check if array contains nulls if we want to ignore them (skip validation for those nulls)
            // But if the array itself contains nulls, typically validation logic might want to filter them out 
            // or fail depending on requirement. 
            // Here, let's assume "ignoreNulls" means we filter out nulls before checking constraints.
            values = Arrays.stream(values).filter(v -> v != null).toArray(Long[]::new);
        } else {
             // If not ignoreNulls, check if any element is null
             for (Long val : values) {
                 if (val == null) return false;
             }
        }

        for (Long val : values) {
            if (val < min || val > max) {
                return false;
            }
        }

        if (unique) {
            Set<Long> set = new HashSet<>(Arrays.asList(values));
            if (set.size() != values.length) {
                return false;
            }
        }

        return true;
    }
}
