
package com.kratosgado.blog.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.kratosgado.blog.utils.exceptions.BlogExceptions;
import com.kratosgado.blog.utils.exceptions.InternalException;

public class ValidationUtils {
  private static final Logger logger = LoggerFactory.getLogger(ValidationUtils.class);

  public static boolean isValidEmail(String email) {
    return email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
  }

  public static boolean isValidPassword(String password) {
    return password.length() >= 8;
  }

  public static String hashPassword(String password) throws InternalException {
    try {
      return BCrypt.withDefaults().hashToString(12, password.toCharArray());
    } catch (Exception e) {
      logger.error("Password hashing failed", e);
      throw BlogExceptions.internal();
    }
  }

  public static boolean verifyPassword(String password, String hash) {
    try {
      return BCrypt.verifyer().verify(password.toCharArray(), hash).verified;
    } catch (Exception e) {
      logger.error("Password verification failed", e);
      return false;
    }
  }

}
