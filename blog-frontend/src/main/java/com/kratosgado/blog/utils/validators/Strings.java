package com.kratosgado.blog.utils.validators;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class Strings {

  @Target(ElementType.RECORD_COMPONENT)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface IsString {
    int minLenth() default 1;

    int maxLenth() default 1000;
  }

  @Target(ElementType.RECORD_COMPONENT)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface IsEmail {

  }

  @Target(ElementType.RECORD_COMPONENT)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface IsStrongPassword {
  }

}
