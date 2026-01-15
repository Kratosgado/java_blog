package com.kratosgado.blog.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.utils.exceptions.BlogException;

import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

/**
 * Service for handling file uploads in the application.
 * Supports image validation, file size limits, and organized storage.
 */
public class UploadService {
  private static final Logger logger = LoggerFactory.getLogger(UploadService.class);

  // File size limits (in bytes)
  private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024; // 5MB
  private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10MB
  private static final long MAX_FILE_SIZE = 20 * 1024 * 1024; // 20MB

  // Image dimension limits
  private static final int MAX_AVATAR_DIMENSION = 1000;
  private static final int MAX_IMAGE_DIMENSION = 5000;

  // Allowed file extensions
  private static final List<String> IMAGE_EXTENSIONS = Arrays.asList(".png", ".jpg", ".jpeg", ".gif", ".svg");
  private static final List<String> DOCUMENT_EXTENSIONS = Arrays.asList(".pdf", ".doc", ".docx", ".txt");

  // Upload directories
  private static final String BASE_UPLOAD_DIR = "uploads";
  private static final String AVATAR_DIR = "uploads/avatars";
  private static final String POST_IMAGE_DIR = "uploads/posts";
  private static final String COVER_IMAGE_DIR = "uploads/covers";
  private static final String DOCUMENT_DIR = "uploads/documents";

  public enum UploadType {
    AVATAR,
    POST_IMAGE,
    COVER_IMAGE,
    DOCUMENT,
    GENERAL
  }

  /**
   * Opens a file chooser dialog for image selection.
   * 
   * @param window The parent window for the dialog
   * @param title  The title of the file chooser dialog
   * @return The selected file, or null if cancelled
   */
  public File chooseImageFile(Window window, String title) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle(title);
    fileChooser.getExtensionFilters().addAll(
        new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.svg"),
        new ExtensionFilter("PNG Files", "*.png"),
        new ExtensionFilter("JPG Files", "*.jpg", "*.jpeg"),
        new ExtensionFilter("GIF Files", "*.gif"),
        new ExtensionFilter("SVG Files", "*.svg"),
        new ExtensionFilter("All Files", "*.*"));
    return fileChooser.showOpenDialog(window);
  }

  /**
   * Opens a file chooser dialog for document selection.
   * 
   * @param window The parent window for the dialog
   * @param title  The title of the file chooser dialog
   * @return The selected file, or null if cancelled
   */
  public File chooseDocumentFile(Window window, String title) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle(title);
    fileChooser.getExtensionFilters().addAll(
        new ExtensionFilter("Documents", "*.pdf", "*.doc", "*.docx", "*.txt"),
        new ExtensionFilter("PDF Files", "*.pdf"),
        new ExtensionFilter("Word Documents", "*.doc", "*.docx"),
        new ExtensionFilter("Text Files", "*.txt"),
        new ExtensionFilter("All Files", "*.*"));
    return fileChooser.showOpenDialog(window);
  }

  /**
   * Opens a file chooser dialog for CSV file selection.
   * 
   * @param window The parent window for the dialog
   * @param title  The title of the file chooser dialog
   * @return The selected file, or null if cancelled
   */
  public File chooseCSVFile(Window window, String title) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle(title);
    fileChooser.getExtensionFilters().addAll(
        new ExtensionFilter("CSV Files", "*.csv"),
        new ExtensionFilter("All Files", "*.*"));
    return fileChooser.showOpenDialog(window);
  }

  /**
   * Validates a file for avatar upload.
   * Checks file extension, size, and image dimensions.
   * 
   * @param file The file to validate
   * @throws BlogException.BadRequestException if validation fails
   */
  public void validateAvatarFile(File file) {
    validateImageFile(file);

    // Check file size
    long fileSize = file.length();
    if (fileSize > MAX_AVATAR_SIZE) {
      throw BlogException.badRequest("Avatar size must be less than " + (MAX_AVATAR_SIZE / (1024 * 1024)) + "MB");
    }

    // Check image dimensions
    try {
      Image image = new Image(file.toURI().toString());
      if (image.getWidth() > MAX_AVATAR_DIMENSION || image.getHeight() > MAX_AVATAR_DIMENSION) {
        throw BlogException.badRequest(
            "Avatar dimensions must be less than " + MAX_AVATAR_DIMENSION + "x" + MAX_AVATAR_DIMENSION + " pixels");
      }
    } catch (Exception e) {
      logger.error("Failed to load image for dimension check", e);
      throw BlogException.badRequest("Failed to validate image dimensions");
    }
  }

  /**
   * Validates a file for general image upload.
   * Checks file extension and size.
   * 
   * @param file The file to validate
   * @throws BlogException.BadRequestException if validation fails
   */
  public void validateImageFile(File file) {
    if (file == null || !file.exists()) {
      throw BlogException.badRequest("File does not exist");
    }

    // Check file extension
    String fileName = file.getName().toLowerCase();
    boolean validExtension = IMAGE_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    if (!validExtension) {
      throw BlogException.badRequest("Invalid image file format. Allowed: " + String.join(", ", IMAGE_EXTENSIONS));
    }

    // Check file size
    long fileSize = file.length();
    if (fileSize > MAX_IMAGE_SIZE) {
      throw BlogException.badRequest("Image size must be less than " + (MAX_IMAGE_SIZE / (1024 * 1024)) + "MB");
    }
  }

  /**
   * Validates a file for document upload.
   * 
   * @param file The file to validate
   * @throws BlogException.BadRequestException if validation fails
   */
  public void validateDocumentFile(File file) {
    if (file == null || !file.exists()) {
      throw BlogException.badRequest("File does not exist");
    }

    // Check file extension
    String fileName = file.getName().toLowerCase();
    boolean validExtension = DOCUMENT_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    if (!validExtension) {
      throw BlogException
          .badRequest("Invalid document file format. Allowed: " + String.join(", ", DOCUMENT_EXTENSIONS));
    }

    // Check file size
    long fileSize = file.length();
    if (fileSize > MAX_FILE_SIZE) {
      throw BlogException.badRequest("File size must be less than " + (MAX_FILE_SIZE / (1024 * 1024)) + "MB");
    }
  }

  /**
   * Uploads a file and returns its URI.
   * 
   * @param filePath   The path to the file to upload
   * @param uploadType The type of upload (determines destination directory and
   *                   validation)
   * @return The URI of the uploaded file
   * @throws BlogException.BadRequestException if upload fails
   */
  public String uploadFile(String filePath, UploadType uploadType) {
    if (filePath == null || filePath.isEmpty()) {
      throw BlogException.badRequest("File path is required");
    }

    File file = new File(filePath);
    return uploadFile(file, uploadType);
  }

  /**
   * Uploads a file and returns its URI.
   * 
   * @param file       The file to upload
   * @param uploadType The type of upload (determines destination directory and
   *                   validation)
   * @return The URI of the uploaded file
   * @throws BlogException.BadRequestException if upload fails
   */
  public String uploadFile(File file, UploadType uploadType) {
    if (file == null || !file.exists()) {
      throw BlogException.badRequest("File does not exist");
    }

    // Validate file based on upload type
    switch (uploadType) {
      case AVATAR:
        validateAvatarFile(file);
        break;
      case POST_IMAGE:
      case COVER_IMAGE:
        validateImageFile(file);
        break;
      case DOCUMENT:
        validateDocumentFile(file);
        break;
      case GENERAL:
        // Basic validation only
        if (file.length() > MAX_FILE_SIZE) {
          throw BlogException.badRequest("File size must be less than " + (MAX_FILE_SIZE / (1024 * 1024)) + "MB");
        }
        break;
    }

    try {
      // Determine upload directory
      String uploadDir = getUploadDirectory(uploadType);
      Path uploadPath = Paths.get(uploadDir);

      // Create directory if it doesn't exist
      if (!Files.exists(uploadPath)) {
        Files.createDirectories(uploadPath);
        logger.info("Created upload directory: {}", uploadDir);
      }

      // Generate unique filename
      String originalFileName = file.getName();
      String fileExtension = originalFileName.substring(originalFileName.lastIndexOf('.'));
      String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

      // Copy file to upload directory
      Path source = file.toPath();
      Path target = uploadPath.resolve(uniqueFileName);
      Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);

      logger.info("File uploaded successfully: {} -> {}", originalFileName, uniqueFileName);
      return target.toUri().toString();

    } catch (IOException e) {
      logger.error("Failed to upload file: {}", file.getAbsolutePath(), e);
      throw BlogException.badRequest("Failed to upload file: " + e.getMessage());
    }
  }

  /**
   * Gets the upload directory for a given upload type.
   * 
   * @param uploadType The type of upload
   * @return The directory path
   */
  private String getUploadDirectory(UploadType uploadType) {
    return switch (uploadType) {
      case AVATAR -> AVATAR_DIR;
      case POST_IMAGE -> POST_IMAGE_DIR;
      case COVER_IMAGE -> COVER_IMAGE_DIR;
      case DOCUMENT -> DOCUMENT_DIR;
      case GENERAL -> BASE_UPLOAD_DIR;
    };
  }

  /**
   * Deletes a file from the upload directory.
   * 
   * @param fileUri The URI of the file to delete
   * @return true if deleted successfully, false otherwise
   */
  public boolean deleteFile(String fileUri) {
    if (fileUri == null || fileUri.isEmpty()) {
      return false;
    }

    try {
      Path filePath = Paths.get(new java.net.URI(fileUri));
      if (Files.exists(filePath)) {
        Files.delete(filePath);
        logger.info("File deleted: {}", fileUri);
        return true;
      } else {
        logger.warn("File not found for deletion: {}", fileUri);
        return false;
      }
    } catch (Exception e) {
      logger.error("Failed to delete file: {}", fileUri, e);
      return false;
    }
  }

  /**
   * Gets the file size in a human-readable format.
   * 
   * @param file The file
   * @return Formatted file size (e.g., "2.5 MB")
   */
  public String getFormattedFileSize(File file) {
    if (file == null || !file.exists()) {
      return "0 B";
    }

    long size = file.length();
    if (size < 1024) {
      return size + " B";
    } else if (size < 1024 * 1024) {
      return String.format("%.2f KB", size / 1024.0);
    } else {
      return String.format("%.2f MB", size / (1024.0 * 1024.0));
    }
  }

  /**
   * Checks if a file is an image based on its extension.
   * 
   * @param file The file to check
   * @return true if the file is an image, false otherwise
   */
  public boolean isImageFile(File file) {
    if (file == null) {
      return false;
    }
    String fileName = file.getName().toLowerCase();
    return IMAGE_EXTENSIONS.stream().anyMatch(fileName::endsWith);
  }

  /**
   * Checks if a file is a document based on its extension.
   * 
   * @param file The file to check
   * @return true if the file is a document, false otherwise
   */
  public boolean isDocumentFile(File file) {
    if (file == null) {
      return false;
    }
    String fileName = file.getName().toLowerCase();
    return DOCUMENT_EXTENSIONS.stream().anyMatch(fileName::endsWith);
  }
}
