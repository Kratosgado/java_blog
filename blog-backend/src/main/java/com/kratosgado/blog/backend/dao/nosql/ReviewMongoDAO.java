package com.kratosgado.blog.backend.dao.nosql;

import com.kratosgado.blog.backend.config.database.MongoDBConfig;
import com.kratosgado.blog.models.Review;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import java.time.LocalDateTime;
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
 * MongoDB-based DAO for Reviews (NoSQL implementation).
 * Demonstrates flexible schema for unstructured feedback data.
 * 
 * Justification for NoSQL:
 * - Reviews may have varying fields (metadata, images, videos)
 * - Flexible schema allows easy addition of new review types
 * - High write throughput for user feedback
 * - Easy horizontal scaling for large review volumes
 */
@Repository
public class ReviewMongoDAO {
  private static final Logger logger = LoggerFactory.getLogger(ReviewMongoDAO.class);
  private static final String COLLECTION_NAME = "reviews";
  
  private final MongoCollection<Document> collection;
  
  public ReviewMongoDAO() {
    MongoDatabase database = MongoDBConfig.getDatabase();
    this.collection = database.getCollection(COLLECTION_NAME);
    createIndexes();
    logger.info("ReviewMongoDAO initialized with collection: {}", COLLECTION_NAME);
  }
  
  /**
   * Create indexes for optimized queries.
   */
  private void createIndexes() {
    try {
      // Index on post_id for fast post review lookups
      collection.createIndex(new Document("post_id", 1));
      
      // Index on user_id for user review history
      collection.createIndex(new Document("user_id", 1));
      
      // Index on rating for filtering/sorting
      collection.createIndex(new Document("rating", -1));
      
      // Compound index for post-specific rating queries
      collection.createIndex(new Document("post_id", 1).append("rating", -1));
      
      logger.info("MongoDB indexes created for reviews collection");
    } catch (Exception e) {
      logger.warn("Error creating indexes (may already exist): {}", e.getMessage());
    }
  }
  
  /**
   * Create a new review in MongoDB.
   */
  public Optional<Review> createReview(Review review) {
    try {
      Document doc = reviewToDocument(review);
      doc.put("created_at", new Date());
      doc.put("updated_at", new Date());
      
      collection.insertOne(doc);
      
      // Get the generated _id and store as string
      String mongoId = doc.getObjectId("_id").toString();
      review.setId(mongoId);
      
      logger.info("Created review in MongoDB with _id: {}", mongoId);
      return Optional.of(review);
    } catch (Exception e) {
      logger.error("Error creating review in MongoDB", e);
      return Optional.empty();
    }
  }
  
  /**
   * Get review by MongoDB ObjectId.
   */
  public Optional<Review> getReviewById(String mongoId) {
    try {
      Document doc = collection.find(Filters.eq("_id", new ObjectId(mongoId))).first();
      if (doc != null) {
        return Optional.of(documentToReview(doc));
      }
      return Optional.empty();
    } catch (Exception e) {
      logger.error("Error getting review by id: {}", mongoId, e);
      return Optional.empty();
    }
  }
  
  /**
   * Get all reviews for a specific post.
   */
  public List<Review> getReviewsByPostId(Long postId) {
    List<Review> reviews = new ArrayList<>();
    try {
      collection.find(Filters.eq("post_id", postId.intValue()))
        .sort(Sorts.descending("created_at"))
        .into(new ArrayList<>())
        .forEach(doc -> reviews.add(documentToReview(doc)));
      
      logger.debug("Found {} reviews for post {}", reviews.size(), postId);
    } catch (Exception e) {
      logger.error("Error getting reviews for post {}", postId, e);
    }
    return reviews;
  }
  
  /**
   * Get all reviews by a specific user.
   */
  public List<Review> getReviewsByUserId(Long userId) {
    List<Review> reviews = new ArrayList<>();
    try {
      collection.find(Filters.eq("user_id", userId.intValue()))
        .sort(Sorts.descending("created_at"))
        .into(new ArrayList<>())
        .forEach(doc -> reviews.add(documentToReview(doc)));
      
      logger.debug("Found {} reviews by user {}", reviews.size(), userId);
    } catch (Exception e) {
      logger.error("Error getting reviews by user {}", userId, e);
    }
    return reviews;
  }
  
  /**
   * Update review.
   */
  public boolean updateReview(String mongoId, Review review) {
    try {
      collection.updateOne(
        Filters.eq("_id", new ObjectId(mongoId)),
        Updates.combine(
          Updates.set("rating", review.getRating()),
          Updates.set("title", review.getTitle()),
          Updates.set("content", review.getContent()),
          Updates.set("helpful", review.isHelpful()),
          Updates.set("updated_at", new Date())
        )
      );
      
      logger.info("Updated review {}", mongoId);
      return true;
    } catch (Exception e) {
      logger.error("Error updating review {}", mongoId, e);
      return false;
    }
  }
  
  /**
   * Delete review.
   */
  public boolean deleteReview(String mongoId) {
    try {
      collection.deleteOne(Filters.eq("_id", new ObjectId(mongoId)));
      logger.info("Deleted review {}", mongoId);
      return true;
    } catch (Exception e) {
      logger.error("Error deleting review {}", mongoId, e);
      return false;
    }
  }
  
  /**
   * Get average rating for a post.
   */
  public double getAverageRatingForPost(Long postId) {
    try {
      List<Review> reviews = getReviewsByPostId(postId);
      if (reviews.isEmpty()) {
        return 0.0;
      }
      
      double sum = reviews.stream().mapToInt(Review::getRating).sum();
      return sum / reviews.size();
    } catch (Exception e) {
      logger.error("Error calculating average rating for post {}", postId, e);
      return 0.0;
    }
  }
  
  /**
   * Get review count for a post.
   */
  public long getReviewCountForPost(Long postId) {
    try {
      return collection.countDocuments(Filters.eq("post_id", postId.intValue()));
    } catch (Exception e) {
      logger.error("Error counting reviews for post {}", postId, e);
      return 0;
    }
  }

  /**
   * Get total review count.
   */
  public long getTotalReviewCount() {
    try {
      return collection.countDocuments();
    } catch (Exception e) {
      logger.error("Error counting total reviews", e);
      return 0;
    }
  }
  
  /**
   * Convert Review object to MongoDB Document.
   */
  private Document reviewToDocument(Review review) {
    Document doc = new Document();
    
    doc.append("post_id", review.getPostId().intValue())
      .append("user_id", review.getUserId().intValue())
      .append("rating", review.getRating())
      .append("title", review.getTitle())
      .append("content", review.getContent())
      .append("helpful", review.isHelpful())
      .append("author_name", review.getAuthorName())
      .append("author_avatar_url", review.getAuthorAvatarUrl());
    
    // MongoDB allows flexible schema - can add custom fields
    doc.append("metadata", new Document()
      .append("verified_purchase", false)
      .append("platform", "Spring Boot")
      .append("version", "1.0")
    );
    
    return doc;
  }
  
  /**
   * Convert MongoDB Document to Review object.
   */
  private Review documentToReview(Document doc) {
    Review review = new Review();
    
    // Use MongoDB ObjectId as string ID
    ObjectId objectId = doc.getObjectId("_id");
    review.setId(objectId.toString());
    
    review.setPostId(Long.valueOf(doc.getInteger("post_id", 0)));
    review.setUserId(Long.valueOf(doc.getInteger("user_id", 0)));
    review.setRating(doc.getInteger("rating", 0));
    review.setTitle(doc.getString("title"));
    review.setContent(doc.getString("content"));
    review.setHelpful(doc.getBoolean("helpful", false));
    review.setAuthorName(doc.getString("author_name"));
    review.setAuthorAvatarUrl(doc.getString("author_avatar_url"));
    
    // Parse timestamps if available
    Date createdAt = doc.getDate("created_at");
    if (createdAt != null) {
      try {
        review.setCreatedAt(LocalDateTime.ofInstant(createdAt.toInstant(), java.time.ZoneId.systemDefault()));
      } catch (Exception e) {
        logger.warn("Error parsing created_at timestamp", e);
      }
    }
    
    Date updatedAt = doc.getDate("updated_at");
    if (updatedAt != null) {
      try {
        review.setUpdatedAt(LocalDateTime.ofInstant(updatedAt.toInstant(), java.time.ZoneId.systemDefault()));
      } catch (Exception e) {
        logger.warn("Error parsing updated_at timestamp", e);
      }
    }
    
    return review;
  }
  
  /**
   * Get all reviews (for testing/debugging).
   */
  public List<Review> getAllReviews() {
    List<Review> reviews = new ArrayList<>();
    try {
      collection.find()
        .sort(Sorts.descending("created_at"))
        .into(new ArrayList<>())
        .forEach(doc -> reviews.add(documentToReview(doc)));
      
      logger.debug("Found {} total reviews", reviews.size());
    } catch (Exception e) {
      logger.error("Error getting all reviews", e);
    }
    return reviews;
  }
}
