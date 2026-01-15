
package com.kratosgado.blog.utils.validators;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class Numbers {

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.RECORD_COMPONENT)
  public @interface Min {
    int value();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.RECORD_COMPONENT)
  public @interface Max {
    int value();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.RECORD_COMPONENT)
  public @interface NotZero {
  }

  public @interface Positive {
  }
}
