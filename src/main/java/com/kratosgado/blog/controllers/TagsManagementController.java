package com.kratosgado.blog.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class TagsManagementController {
  private static final Logger logger = LoggerFactory.getLogger(TagsManagementController.class);

  @FXML
  private VBox tagFormContainer;

  @FXML
  private Label formTitleLabel;

  @FXML
  private MFXTextField tagNameField;

  @FXML
  private MFXTextField tagSlugField;

  @FXML
  private MFXTextField tagDescriptionField;

  @FXML
  private MFXButton createTagBtn;

  // @FXML
  // private MFXButton saveBtn;

  @FXML
  private MFXButton cancelFormBtn;

  // @FXML
  // private MFXButton deleteBtn;

  @FXML
  private VBox tagsListContainer;

  @FXML
  private void initialize() {
    logger.debug("Initializing Tags Management Controller");
    setupUI();
    loadTags();
  }

  private void setupUI() {
    createTagBtn.setOnAction(e -> showCreateForm());
    // saveBtn.setOnAction(e -> saveTag());
    cancelFormBtn.setOnAction(e -> cancelForm());
    // deleteBtn.setOnAction(e -> deleteTag());
  }

  private void loadTags() {
    try {
      logger.info("Loading tags");
    } catch (Exception e) {
      logger.error("Failed to load tags", e);
    }
  }

  private void showCreateForm() {
    tagFormContainer.setVisible(true);
    tagFormContainer.setManaged(true);
    formTitleLabel.setText("Create New Tag");
    clearForm();
    logger.info("Show create tag form");
  }

  private void saveTag() {
    if (validateForm()) {
      logger.info("Saving tag: {}", tagNameField.getText());
      cancelForm();
    }
  }

  private void cancelForm() {
    tagFormContainer.setVisible(false);
    tagFormContainer.setManaged(false);
    clearForm();
  }

  private void deleteTag() {
    logger.info("Deleting tag");
  }

  private void clearForm() {
    tagNameField.clear();
    tagSlugField.clear();
    tagDescriptionField.clear();
  }

  private boolean validateForm() {
    if (tagNameField.getText().isEmpty()) {
      logger.warn("Tag name is required");
      return false;
    }
    return true;
  }
}
