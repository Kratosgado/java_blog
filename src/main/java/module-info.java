module com.kratosgado.blog {
  requires javafx.controls;
  requires javafx.fxml;
  requires MaterialFX;
  requires java.sql;
  requires static lombok;
  requires java.prefs;
  requires com.google.gson;
  requires io.github.cdimascio.dotenv.java;
  requires org.slf4j;
  requires bcrypt;

  opens com.kratosgado.blog.controllers to javafx.fxml;
  opens com.kratosgado.blog.dao to java.sql;
  opens com.kratosgado.blog.models to com.google.gson;

  exports com.kratosgado.blog;
}
