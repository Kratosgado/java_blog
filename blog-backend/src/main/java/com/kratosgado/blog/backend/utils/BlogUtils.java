package com.kratosgado.blog.backend.utils;

public class BlogUtils {

  private BlogUtils() {
  }

  public static String toSlug(String input) {
    if (input == null) {
      return null;
    }
    return input.toLowerCase()
        .replaceAll("[^a-z0-9\\s-]", "")
        .replaceAll("\\s+", "-")
        .replaceAll("-+", "-")
        .replaceAll("^-|-$", "");
  }

  public static Double round(Double value) {
    return Math.round(value * 100.0) / 100.0;
  }

  public static Long[] toLongArray(Long[] input) {
    return input;
  }
}
