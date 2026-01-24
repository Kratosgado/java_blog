package com.kratosgado.blog.backend.utils;

public class BlogUtils {

  private BlogUtils() {
    // Private constructor to prevent instantiation
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
}
