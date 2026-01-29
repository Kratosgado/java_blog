package com.kratosgado.blog.backend.config.database;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

@Configuration
public class MongoDriverConfig {

  @Bean(destroyMethod = "close")
  public MongoClient mongoClient(Environment env) {
    // Prefer full URI if provided; otherwise build from host/port.
    String uri = firstNonBlank(
        env.getProperty("MONGO_URI"),
        env.getProperty("spring.data.mongodb.uri"));

    if (uri == null) {
      String host = env.getProperty("spring.data.mongodb.host", "localhost");
      String port = env.getProperty("spring.data.mongodb.port", "27017");
      uri = "mongodb://" + host + ":" + port;
    }

    // Validate format early (throws if invalid)
    ConnectionString connectionString = new ConnectionString(uri);
    // keep reference to avoid "ignored instance" warnings
    if (connectionString.getConnectionString() == null) {
      throw new IllegalStateException("Invalid MongoDB connection string");
    }
    return MongoClients.create(uri);
  }

  @Bean
  public MongoDatabase mongoDatabase(MongoClient mongoClient, Environment env) {
    String dbName = firstNonBlank(
        env.getProperty("MONGO_DB_NAME"),
        env.getProperty("spring.data.mongodb.database"));

    if (dbName == null) {
      dbName = "blog_nosql";
    }

    return mongoClient.getDatabase(dbName);
  }

  private static String firstNonBlank(String... values) {
    if (values == null) {
      return null;
    }
    for (String v : values) {
      if (v != null && !v.isBlank()) {
        return v;
      }
    }
    return null;
  }
}
