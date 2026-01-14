
package com.kratosgado.blog.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UiUtils {
  public static String formatDate(LocalDateTime date) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    return date.format(formatter);
  }

  public static String formateDateString(String dateString) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    return LocalDateTime.parse(dateString).format(formatter);
  }

}
