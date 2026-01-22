package com.kratosgado.blog.backend.dao.nosql;

import com.kratosgado.blog.backend.config.database.MongoDBConfig;
import com.kratosgado.blog.models.Comment;
import com.kratosgado.blog.enums.CommentStatus;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * MongoDB-based DAO for Comments (NoSQL implementation).
 * Demonstrates flexible schema for unstructured user feedback.
 * 
 * Justification for NoSQL:
 * - Comments have varying structure (text, images, reactions, mentions)
 * - Threaded/nested comments easier to store as nested documents
 * - High write volume (users comment frequently)
 * - Flexible moderation metadata (spam scores, flagged reasons, etc.)
 * - Easy to add rich features (reactions, mentions, attachments) without schema
 * changes
 */
@Repository
public class CommentMongoDAO {
  private static final Logger logger = LoggerFactory.getLogger(CommentMongoDAO.class);
  private static final String COLLECTION_NAME = "comments";

  private final MongoCollection<Document> collection;

  public CommentMongoDAO() {
    MongoDatabase database = MongoDBConfig.getDatabase();
    this.collection = database.getCollection(COLLECTION_NAME);
    createIndexes();
    logger.info("CommentMongoDAO initialized with collection: {}", COLLECTION_NAME);
  }

  /**
   * Create indexes for optimized queries.
   */
  private void createIndexes() {
    try {
      // Index on post_id for fast post comment lookups
      collection.createIndex(new Document("post_id", 1));

      // Index on user_id for user comment history
      collection.createIndex(new Document("user_id", 1));

      // Index on status for moderation queries
      collection.createIndex(new Document("status", 1));

      // Index on created_at for chronological sorting
      collection.createIndex(new Document("created_at", -1));

      // Compound index for post-specific queries
      collection.createIndex(new Document("post_id", 1).append("status", 1).append("created_at", -1));

      // Index on parent_id for threaded comments
      collection.createIndex(new Document("parent_id", 1));

      logger.info("MongoDB indexes created for comments collection");
    } catch (Exception e) {
      logger.warn("Error creating indexes (may already exist): {}", e.getMessage());
    }
  }

  /**
   * Create a new comment in MongoDB.
   */
  public Optional<Comment> createComment(Comment comment) {
    try {
      Document doc = commentToDocument(comment);
      doc.put("created_at", new Date());
      doc.put("updated_at", new Date());

      collection.insertOne(doc);

      // Get the generated _id
      String mongoId = doc.getObjectId("_id").toString();
      comment.setId(mongoId); // Store MongoDB ObjectId as string

      logger.info("Created comment in MongoDB with _id: {}", mongoId);
      return Optional.of(comment);
    } catch (Exception e) {
      logger.error("Error creating comment in MongoDB", e);
      return Optional.empty();
    }
  }

  /**
   * Get comment by MongoDB ObjectId.
   */
  public Optional<Comment> getCommentById(String mongoId) {
    try {
      Document doc = collection.find(Filters.eq("_id", new ObjectId(mongoId))).first();
      if (doc != null) {
        return Optional.of(documentToComment(doc));
      }
      return Optional.empty();
    } catch (Exception e) {
      logger.error("Error getting comment by id: {}", mongoId, e);
      return Optional.empty();
    }
  }

  /**
   * Get all comments for a specific post.
   */
  public List<Comment> getCommentsByPostId(Long postId) {
    List<Comment> comments = new ArrayList<>();
    try {
      collection.find(Filters.eq("post_id", postId.intValue()))
          .sort(Sorts.descending("created_at"))
          .into(new ArrayList<>())
          .forEach(doc -> comments.add(documentToComment(doc)));

      logger.debug("Found {} comments for post {}", comments.size(), postId);
    } catch (Exception e) {
      logger.error("Error getting comments for post {}", postId, e);
    }
    return comments;
  }

  /**
   * Get comments by status (for moderation).
   */
  public List<Comment> getCommentsByStatus(String status) {
    List<Comment> comments = new ArrayList<>();
    try {
      collection.find(Filters.eq("status", status))
          .sort(Sorts.descending("created_at"))
          .into(new ArrayList<>())
          .forEach(doc -> comments.add(documentToComment(doc)));

      logger.debug("Found {} comments with status {}", comments.size(), status);
    } catch (Exception e) {
      logger.error("Error getting comments by status {}", status, e);
    }
    return comments;
  }

  /**
   * Get all comments by a specific user.
   */
  public List<Comment> getCommentsByUserId(Long userId) {
    List<Comment> comments = new ArrayList<>();
    try {
      collection.find(Filters.eq("user_id", userId.intValue()))
          .sort(Sorts.descending("created_at"))
          .into(new ArrayList<>())
          .forEach(doc -> comments.add(documentToComment(doc)));

      logger.debug("Found {} comments by user {}", comments.size(), userId);
    } catch (Exception e) {
      logger.error("Error getting comments by user {}", userId, e);
    }
    return comments;
  }

  /**
   * Update comment.
   */
  public boolean updateComment(String mongoId, Comment comment) {
    try {
      collection.updateOne(
          Filters.eq("_id", new ObjectId(mongoId)),
          Updates.combine(
              Updates.set("content", comment.getContent()),
              Updates.set("status", comment.getStatus().name()),
              Updates.set("updated_at", new Date())));

      logger.info("Updated comment {}", mongoId);
      return true;
    } catch (Exception e) {
      logger.error("Error updating comment {}", mongoId, e);
      return false;
    }
  }

  /**
   * Update comment status (for moderation).
   */
  public boolean updateCommentStatus(String mongoId, String status) {
    try {
      collection.updateOne(
          Filters.eq("_id", new ObjectId(mongoId)),
          Updates.combine(
              Updates.set("status", status),
              Updates.set("updated_at", new Date())));

      logger.info("Updated comment {} status to {}", mongoId, status);
      return true;
    } catch (Exception e) {
      logger.error("Error updating comment status {}", mongoId, e);
      return false;
    }
  }

  /**
   * Delete comment.
   */
  public boolean deleteComment(String mongoId) {
    try {
      collection.deleteOne(Filters.eq("_id", new ObjectId(mongoId)));
      logger.info("Deleted comment {}", mongoId);
      return true;
    } catch (Exception e) {
      logger.error("Error deleting comment {}", mongoId, e);
      return false;
    }
  }

  /**
   * Get comment count for a post.
   */
  public long getCommentCountForPost(Long postId) {
    try {
      return collection.countDocuments(Filters.eq("post_id", postId.intValue()));
    } catch (Exception e) {
      logger.error("Error counting comments for post {}", postId, e);
      return 0;
    }
  }

  /**
   * Get total comment count.
   */
  public long getTotalCommentCount() {
    try {
      return collection.countDocuments();
    } catch (Exception e) {
      logger.error("Error counting total comments", e);
      return 0;
    }
  }

  /**
   * Convert Comment object to MongoDB Document.
   */
  private Document commentToDocument(Comment comment) {
    Document doc = new Document();

    doc.append("post_id", comment.getPostId())
        .append("user_id", comment.getUserId())
        .append("content", comment.getContent())
        .append("status", comment.getStatus() != null ? comment.getStatus().name() : "PENDING")
        .append("author_name", comment.getAuthorName())
        .append("author_avatar_url", comment.getAuthorAvatarUrl());

    // MongoDB allows flexible schema - can add rich features
    doc.append("metadata", new Document()
        .append("platform", "Spring Boot")
        .append("version", "1.0"));

    return doc;
  }

  /**
   * Convert MongoDB Document to Comment object.
   */
  private Comment documentToComment(Document doc) {
    Comment comment = new Comment();

    // Use MongoDB ObjectId as string ID
    ObjectId objectId = doc.getObjectId("_id");
    comment.setId(objectId.toString());

    comment.setPostId(doc.getLong("post_id"));
    comment.setUserId(doc.getLong("user_id"));
    comment.setContent(doc.getString("content"));

    // Parse status enum
    String statusStr = doc.getString("status");
    if (statusStr != null) {
      try {
        comment.setStatus(CommentStatus.valueOf(statusStr));
      } catch (Exception e) {
        comment.setStatus(CommentStatus.pending);
      }
    }

    comment.setAuthorName(doc.getString("author_name"));
    comment.setAuthorAvatarUrl(doc.getString("author_avatar_url"));

    // Parse timestamps
    Date createdAt = doc.getDate("created_at");
    if (createdAt != null) {
      try {
        comment.setCreatedAt(LocalDateTime.ofInstant(createdAt.toInstant(), ZoneId.systemDefault()));
      } catch (Exception e) {
        logger.warn("Error parsing created_at timestamp", e);
      }
    }

    Date updatedAt = doc.getDate("updated_at");
    if (updatedAt != null) {
      try {
        comment.setUpdatedAt(LocalDateTime.ofInstant(updatedAt.toInstant(), ZoneId.systemDefault()));
      } catch (Exception e) {
        logger.warn("Error parsing updated_at timestamp", e);
      }
    }

    return comment;
  }

  /**
   * Get all comments (for testing/debugging).
   */
  public List<Comment> getAllComments() {
    List<Comment> comments = new ArrayList<>();
    try {
      collection.find()
          .sort(Sorts.descending("created_at"))
          .into(new ArrayList<>())
          .forEach(doc -> comments.add(documentToComment(doc)));

      logger.debug("Found {} total comments", comments.size());
    } catch (Exception e) {
      logger.error("Error getting all comments", e);
    }
    return comments;
  }
}
