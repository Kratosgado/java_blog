package com.kratosgado.blog.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kratosgado.blog.dtos.request.CreateTagDto;
import com.kratosgado.blog.models.Tag;
import com.kratosgado.blog.services.TagService;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
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
  private TextArea tagDescriptionArea;

  @FXML
  private MFXButton saveTagBtn;

  @FXML
  private MFXButton cancelFormBtn;

  @FXML
  private VBox tagsListContainer;

  private final TagService tagService;
  private Tag currentTag;

  public TagsManagementController() {
    this.tagService = new TagService();
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
  }

  private void loadTags() {
    try {
      tagsListContainer.getChildren().clear();
      var tags = tagService.getAllTags();
      logger.info("Loading {} tags", tags.size());

      for (Tag tag : tags) {
        HBox tagItem = createTagItem(tag);
        tagsListContainer.getChildren().add(tagItem);
      }
    } catch (Exception e) {
      logger.error("Failed to load tags", e);
    }
  }

  private HBox createTagItem(Tag tag) {
    HBox container = new HBox(15);
    container.setAlignment(Pos.CENTER_LEFT);
    container.setStyle(
        "-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #e0e0e0; -fx-border-radius: 8;");

    VBox tagInfo = new VBox(5);
    Label tagNameLabel = new Label(tag.getName());
    tagNameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
    Label tagSlugLabel = new Label("Slug: " + tag.getSlug());
    tagSlugLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");
    Label postCountLabel = new Label(tag.getPostCount() + " posts");
    postCountLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #999;");

    tagInfo.getChildren().addAll(tagNameLabel, tagSlugLabel, postCountLabel);

    HBox spacer = new HBox();
    HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

    MFXButton editBtn = new MFXButton("Edit");
    editBtn
        .setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 16;");
    editBtn.setOnAction(e -> editTag(tag));

    MFXButton deleteBtn = new MFXButton("Delete");
    deleteBtn
        .setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 16;");
    deleteBtn.setOnAction(e -> deleteTag(tag.getId()));

    container.getChildren().addAll(tagInfo, spacer, editBtn, deleteBtn);
    return container;
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
      CreateTagDto dto = new CreateTagDto(tagNameField.getText(), tagDescriptionArea.getText());

      boolean success = currentTag == null ? tagService.createTag(dto) : tagService.updateTag(currentTag.getId(), dto);

      if (success) {
        logger.info("Saving tag: {}", dto.name());
        loadTags();
        cancelForm();
      }
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
    tagSlugField.setText(tag.getSlug());
    tagDescriptionArea.setText(tag.getDescription());
    logger.info("Editing tag: {}", tag.getId());
  }

  private void deleteTag(int tagId) {
    try {
      if (tagService.deleteTag(tagId)) {
        logger.info("Deleting tag: {}", tagId);
        loadTags();
      }
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
    tagSlugField.clear();
    tagDescriptionArea.clear();
  }

}
