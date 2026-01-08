package com.kratosgado.blog.utils;

import javafx.scene.image.Image;

/**
 * Utility class for loading images with fallback to default logo
 */
public class ImageUtils {

  private static final String DEFAULT_AVATAR_PATH = "src/main/resources/images/java_blog_logo.jpg";

  /**
   * Loads an image from URL, with fallback to default avatar if loading fails
   * 
   * @param imageUrl URL of the image to load
   * @return Image object, either the loaded image or default avatar
   */
  public static Image loadImageWithFallback(String imageUrl) {
    if (imageUrl == null || imageUrl.trim().isEmpty()) {
      return loadDefaultAvatar();
    }
    try {
      return new Image(imageUrl, true);

    } catch (Exception e) {
      System.err.println("Failed to load image from URL: " + imageUrl + ", using default avatar");
      return loadDefaultAvatar();
    }
  }

  /**
   * Loads the default avatar image
   * 
   * @return Default avatar image
   */
  public static Image loadDefaultAvatar() {
    try {
      return new Image("file:" + DEFAULT_AVATAR_PATH, true);
    } catch (Exception e) {
      System.err.println("Failed to load default avatar: " + e.getMessage());
      // Return a blank/transparent image as last resort
      return new Image(
          "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==");
    }
  }

  /**
   * Checks if a URL is likely a local file path
   * 
   * @param url URL to check
   * @return true if it's a file path
   */
  private static boolean isFilePath(String url) {
    return url != null && (url.startsWith("file:") || url.startsWith("/") || url.contains(":/"));
  }
}
