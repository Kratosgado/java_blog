package com.kratosgado.blog.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

public class PostsListController {
  private static final Logger logger = LoggerFactory.getLogger(PostsListController.class);

  @FXML
  private MFXTextField searchField;

  @FXML
  private MFXComboBox<String> sortComboBox;

  @FXML
  private MFXComboBox<String> filterComboBox;

  @FXML
  private MFXButton createNewPostBtn;

  @FXML
  private MFXButton searchBtn;

  @FXML
  private MFXButton clearBtn;

  @FXML
  private TableView<?> postsTable;

  @FXML
  private Label pageLabel;

  @FXML
  private MFXButton prevPageBtn;

  @FXML
  private MFXButton nextPageBtn;

  @FXML
  private void initialize() {
    logger.debug("Initializing Posts List Controller");
    setupUI();
    loadPosts();
  }

  private void setupUI() {
    searchBtn.setOnAction(e -> searchPosts());
    clearBtn.setOnAction(e -> clearSearch());
    createNewPostBtn.setOnAction(e -> createNewPost());
    prevPageBtn.setOnAction(e -> previousPage());
    nextPageBtn.setOnAction(e -> nextPage());

    sortComboBox.getItems().addAll("Latest", "Oldest", "Most Viewed", "Title A-Z");
    filterComboBox.getItems().addAll("All", "Published", "Draft");
  }

  private void loadPosts() {
    try {
      pageLabel.setText("Page 1 of 1");
      logger.info("Posts loaded successfully");
    } catch (Exception e) {
      logger.error("Failed to load posts", e);
    }
  }

  private void searchPosts() {
    String query = searchField.getText();
    logger.info("Searching posts with query: {}", query);
  }

  private void clearSearch() {
    searchField.clear();
    loadPosts();
  }

  private void createNewPost() {
    logger.info("Creating new post");
  }

  private void previousPage() {
    logger.info("Previous page clicked");
  }

  private void nextPage() {
    logger.info("Next page clicked");
  }
}
