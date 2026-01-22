package com.kratosgado.blog.backend.config.database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MongoDB connection configuration for NoSQL data storage.
 * Used for storing unstructured data like reviews and comments.
 */
public class MongoDBConfig {
  private static final Logger logger = LoggerFactory.getLogger(MongoDBConfig.class);
  private static MongoClient mongoClient;
  private static MongoDatabase database;
  
  // MongoDB connection settings
  private static final String MONGO_URI = System.getenv().getOrDefault(
    "MONGO_URI", "mongodb://localhost:27017"
  );
  private static final String DATABASE_NAME = System.getenv().getOrDefault(
    "MONGO_DB_NAME", "blog_nosql"
  );
  
  /**
   * Get MongoDB client instance (singleton).
   */
  public static synchronized MongoClient getClient() {
    if (mongoClient == null) {
      try {
        logger.info("Connecting to MongoDB at {}", MONGO_URI);
        mongoClient = MongoClients.create(MONGO_URI);
        logger.info("MongoDB connection established successfully");
      } catch (Exception e) {
        logger.error("Failed to connect to MongoDB: {}", e.getMessage(), e);
        throw new RuntimeException("MongoDB connection failed", e);
      }
    }
    return mongoClient;
  }
  
  /**
   * Get MongoDB database instance.
   */
  public static synchronized MongoDatabase getDatabase() {
    if (database == null) {
      database = getClient().getDatabase(DATABASE_NAME);
      logger.info("Using MongoDB database: {}", DATABASE_NAME);
    }
    return database;
  }
  
  /**
   * Close MongoDB connection.
   */
  public static void close() {
    if (mongoClient != null) {
      logger.info("Closing MongoDB connection");
      mongoClient.close();
      mongoClient = null;
      database = null;
    }
  }
  
  /**
   * Test MongoDB connection.
   */
  public static boolean testConnection() {
    try {
      MongoDatabase db = getDatabase();
      db.listCollectionNames().first(); // Test operation
      logger.info("MongoDB connection test successful");
      return true;
    } catch (Exception e) {
      logger.error("MongoDB connection test failed: {}", e.getMessage());
      return false;
    }
  }
}
