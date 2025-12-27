
package com.kratosgado.blog.utils.validators;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.utils.exceptions.BadRequestException;
import com.kratosgado.blog.utils.exceptions.BlogExceptions;
import com.kratosgado.blog.utils.validators.Numbers.Min;
import com.kratosgado.blog.utils.validators.Objects.NotNull;
import com.kratosgado.blog.utils.validators.Strings.IsEmail;
import com.kratosgado.blog.utils.validators.Strings.IsString;
import com.kratosgado.blog.utils.validators.Strings.IsStrongPassword;

public class Validator {
  private static Logger logger = LoggerFactory.getLogger(Validator.class);

  public static <T extends Record> void validate(T record) {
    for (RecordComponent field : record.getClass().getRecordComponents()) {
      try {
        Object value = field.getAccessor().invoke(record);
        // logger.info("Validating field: {} {}", field.getName(), value);

        if (field.isAnnotationPresent(NotNull.class))
          validateNotNull(field, value);

        if (field.isAnnotationPresent(Min.class))
          validateMin(field, value);

        if (field.isAnnotationPresent(IsString.class))
          validateString(field, value);

        if (field.isAnnotationPresent(IsEmail.class))
          validateEmail(field, value);

        if (field.isAnnotationPresent(IsStrongPassword.class))
          validateStrongPassword(field, value);

      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }

    }
  }

  private static void validateNotNull(RecordComponent field, Object value) {
    if (value == null) {
      throw new BadRequestException(field.getName() + " cannot be null");
    }
  }

  private static void validateString(RecordComponent field, Object value) {
    if (!(value instanceof String)) {
      throw new BadRequestException(field.getName() + " must be a string");
    }
    String str = (String) value;
    IsString annotation = field.getAnnotation(IsString.class);
    if (str.length() < annotation.minLenth() || str.length() > annotation.maxLenth()) {
      throw new BadRequestException(field.getName() + " must be between " + annotation.minLenth() + " and "
          + annotation.maxLenth() + " characters");
    }
  }

  private static void validateMin(RecordComponent field, Object value) {
    int min = field.getAnnotation(Min.class).value();
    if (value instanceof Number num && num.doubleValue() < min) {
      throw BlogExceptions.badRequest(field.getName() + " must be at least " + min);
    }
  }

  private static void validateEmail(RecordComponent field, Object value) {
    String email = (String) value;
    validateNotNull(field, email);
    validateNotEmpty(field, email);
    if (!ValidationUtils.isValidEmail(email))
      throw BlogExceptions.badRequest(field.getName() + " must be a valid email");
  }

  private static void validateStrongPassword(RecordComponent field, Object value) {
    String password = (String) value;
    validateNotNull(field, password);
    validateNotEmpty(field, password);
    if (!ValidationUtils.isValidPassword(password))
      throw BlogExceptions.badRequest(field.getName() + " must be a strong password");
  }

  private static void validateNotEmpty(RecordComponent field, Object value) {
    String str = (String) value;
    if (str.isEmpty()) {
      throw BlogExceptions.badRequest(field.getName() + " cannot be empty");
    }
  }

}
