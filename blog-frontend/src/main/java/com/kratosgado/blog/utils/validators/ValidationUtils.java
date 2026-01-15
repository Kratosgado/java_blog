
package com.kratosgado.blog.utils.validators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.utils.exceptions.BlogException;
import com.kratosgado.blog.utils.exceptions.InternalException;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class ValidationUtils {
  private static final Logger logger = LoggerFactory.getLogger(ValidationUtils.class);

  public static boolean isValidEmail(String email) {
    return email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
  }

  public static boolean isValidPassword(String password) {
    if (password == null || password.length() < 8) {
      return false;
    }
    boolean hasUpperCase = password.chars().anyMatch(Character::isUpperCase);
    boolean hasLowerCase = password.chars().anyMatch(Character::isLowerCase);
    boolean hasDigit = password.chars().anyMatch(Character::isDigit);
    boolean hasSpecial = password.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));
    // Password must have at least 3 out of 4 characteristics
    int characteristics = 0;
    if (hasUpperCase)
      characteristics++;
    if (hasLowerCase)
      characteristics++;
    if (hasDigit)
      characteristics++;
    if (hasSpecial)
      characteristics++;

    return characteristics >= 3;
  }

  public static String hashPassword(String password) throws InternalException {
    try {
      return BCrypt.withDefaults().hashToString(12, password.toCharArray());
    } catch (Exception e) {
      logger.error("Password hashing failed", e);
      throw BlogException.internal();
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
