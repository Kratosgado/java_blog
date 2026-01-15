module com.kratosgado.blog {
  // Internal modules
  requires com.kratosgado.blog.common;

  requires javafx.controls;
  requires javafx.fxml;
  requires javafx.base;
  requires java.sql;
  requires java.prefs;
  requires static lombok;
  requires com.google.gson;
  requires io.github.cdimascio.dotenv.java;
  requires org.slf4j;
  requires bcrypt;
  requires javafx.graphics;
  requires com.google.guice;
  requires javax.inject;

  // HTTP client for REST API
  requires okhttp3;

  // MongoDB for NoSQL reviews storage
  requires org.mongodb.driver.sync.client;
  requires org.mongodb.bson;
  requires org.mongodb.driver.core;

  opens com.kratosgado.blog.controllers to javafx.fxml, com.google.guice;
  opens com.kratosgado.blog.services to com.google.guice;
  opens com.kratosgado.blog.config to com.google.guice;

  exports com.kratosgado.blog;
}
