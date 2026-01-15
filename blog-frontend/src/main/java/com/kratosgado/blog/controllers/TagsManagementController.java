package com.kratosgado.blog.controllers;

import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.models.Tag;
import com.kratosgado.blog.services.TagService;

import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class TagsManagementController {
  private static final Logger logger = LoggerFactory.getLogger(TagsManagementController.class);

  @FXML
  private Button createTagBtn;

  @FXML
  private VBox tagFormContainer;

  @FXML
  private Label formTitleLabel;

  @FXML
  private TextField tagNameField;

  @FXML
  private ColorPicker tagColorPicker;

  @FXML
  private TextArea tagDescriptionArea;

  @FXML
  private Button saveTagBtn;

  @FXML
  private Button cancelFormBtn;

  @FXML
  private FlowPane tagsFlowPane;

  private final TagService tagService;
  private Tag currentTag;

  @Inject
  public TagsManagementController(TagService tagService) {
    this.tagService = tagService;
  }

  @FXML
  private void initialize() {
    logger.debug("Initializing Tags Management Controller");
    setupUI();
    loadTags();
  }

  private void setupUI() {
    saveTagBtn.setOnAction(e -> saveTag());
    cancelFormBtn.setOnAction(e -> cancelForm());
    
    // Set default color
    if (tagColorPicker != null) {
      tagColorPicker.setValue(Color.web("#6b7280"));
    }
  }

  private void loadTags() {
    try {
      tagsFlowPane.getChildren().clear();
      var tags = tagService.getAllTags();
      logger.info("Loading {} tags", tags.size());

      for (Tag tag : tags) {
        VBox tagCard = createTagCard(tag);
        tagsFlowPane.getChildren().add(tagCard);
      }
    } catch (Exception e) {
      logger.error("Failed to load tags", e);
    }
  }

  private VBox createTagCard(Tag tag) {
    VBox card = new VBox(12);
    card.setAlignment(Pos.TOP_LEFT);
    card.setPrefWidth(200);
    card.setStyle(
        "-fx-background-color: white; " +
        "-fx-padding: 20; " +
        "-fx-background-radius: 12; " +
        "-fx-border-color: #e5e7eb; " +
        "-fx-border-radius: 12; " +
        "-fx-border-width: 1; " +
        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");

    // Tag name with color indicator
    HBox headerBox = new HBox(10);
    headerBox.setAlignment(Pos.CENTER_LEFT);
    
    Label colorIndicator = new Label("●");
    colorIndicator.setStyle("-fx-font-size: 20px; -fx-text-fill: #6b7280;");
    
    Label tagNameLabel = new Label(tag.getName());
    tagNameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #111827;");
    
    headerBox.getChildren().addAll(colorIndicator, tagNameLabel);

    // Slug
    Label tagSlugLabel = new Label(tag.getSlug());
    tagSlugLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

    // Description (if exists)
    VBox contentBox = new VBox(8);
    if (tag.getDescription() != null && !tag.getDescription().isEmpty()) {
      Label descLabel = new Label(tag.getDescription());
      descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #374151; -fx-wrap-text: true;");
      descLabel.setWrapText(true);
      descLabel.setMaxWidth(160);
      contentBox.getChildren().add(descLabel);
    }

    // Post count
    Label postCountLabel = new Label(tag.getPostCount() + " posts");
    postCountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ca3af; -fx-font-style: italic;");

    // Actions
    HBox actionsBox = new HBox(8);
    actionsBox.setAlignment(Pos.CENTER);

    Button editBtn = new Button("Edit");
    editBtn.setStyle(
        "-fx-background-color: -primary-color; " +
        "-fx-text-fill: white; " +
        "-fx-background-radius: 6; " +
        "-fx-padding: 6 12; " +
        "-fx-cursor: hand; " +
        "-fx-font-size: 12px;");
    editBtn.setOnAction(e -> editTag(tag));

    Button deleteBtn = new Button("Delete");
    deleteBtn.setStyle(
        "-fx-background-color: #374151; " +
        "-fx-text-fill: white; " +
        "-fx-background-radius: 6; " +
        "-fx-padding: 6 12; " +
        "-fx-cursor: hand; " +
        "-fx-font-size: 12px;");
    deleteBtn.setOnAction(e -> deleteTag(tag.getId()));

    actionsBox.getChildren().addAll(editBtn, deleteBtn);

    card.getChildren().addAll(headerBox, tagSlugLabel, contentBox, postCountLabel, actionsBox);
    return card;
  }

  @FXML
  private void showCreateForm() {
    tagFormContainer.setVisible(true);
    tagFormContainer.setManaged(true);
    formTitleLabel.setText("Create New Tag");
    clearForm();
    currentTag = null;
    logger.info("Show create tag form");
  }

  private void saveTag() {
    try {
      String name = tagNameField.getText();
      String description = tagDescriptionArea.getText();
      
      if (name == null || name.trim().isEmpty()) {
        logger.warn("Tag name is required");
        return;
      }

      // TODO: Implement tag creation/update via API
      logger.warn("Tag creation/update not yet implemented via API");
      com.kratosgado.blog.utils.DialogUtils.showError("Not Available", "Tag management not yet available");
    } catch (Exception ex) {
      logger.error("Failed to save tag", ex);
    }
  }

  private void editTag(Tag tag) {
    currentTag = tag;
    tagFormContainer.setVisible(true);
    tagFormContainer.setManaged(true);
    formTitleLabel.setText("Edit Tag");
    tagNameField.setText(tag.getName());
    tagDescriptionArea.setText(tag.getDescription());
    
    // Note: Color picker functionality can be added when Tag model supports it
    
    logger.info("Editing tag: {}", tag.getId());
  }

  private void deleteTag(int tagId) {
    try {
      tagService.deleteTag(Long.valueOf(tagId));
      logger.info("Deleting tag: {}", tagId);
      loadTags();
    } catch (Exception ex) {
      logger.error("Failed to delete tag", ex);
    }
  }

  private void cancelForm() {
    tagFormContainer.setVisible(false);
    tagFormContainer.setManaged(false);
    clearForm();
  }

  private void clearForm() {
    tagNameField.clear();
    tagDescriptionArea.clear();
    if (tagColorPicker != null) {
      tagColorPicker.setValue(Color.web("#6b7280"));
    }
  }
}
