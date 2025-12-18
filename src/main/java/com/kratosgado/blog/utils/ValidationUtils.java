
package com.kratosgado.blog.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.kratosgado.blog.utils.exceptions.BlogExceptions;
import com.kratosgado.blog.utils.exceptions.InternalException;

public class ValidationUtils {

  public static boolean isValidEmail(String email) {
    return email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
  }

  public static boolean isValidPassword(String password) {
    return password.length() >= 8;
  }

  public static String hashPassword(String password) throws InternalException {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(password.getBytes());
      StringBuilder hexString = new StringBuilder();
      for (byte b : hash) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1)
          hexString.append('0');
        hexString.append(hex);
      }
      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      throw BlogExceptions.internal();
    }
  }

}
