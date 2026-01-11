module com.kratosgado.blog {
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

  opens com.kratosgado.blog.controllers to javafx.fxml, com.google.guice;
  opens com.kratosgado.blog.dao to java.sql, com.google.guice;
  opens com.kratosgado.blog.services to com.google.guice;
  opens com.kratosgado.blog.models to com.google.gson, javafx.base;

  exports com.kratosgado.blog;
}

