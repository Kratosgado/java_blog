
package com.kratosgado.blog.utils;

public class ValidationUtils {

  public static boolean isValidEmail(String email) {
    return email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
  }

  public static boolean isValidPassword(String password) {
    return password.length() >= 8;
  }
}
