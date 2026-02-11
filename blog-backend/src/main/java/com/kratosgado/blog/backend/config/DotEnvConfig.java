package com.kratosgado.blog.backend.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/** Initializer to load environment variables from .env file into Spring Environment */
public class DotEnvConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    try {
      // Try multiple possible locations for .env file
      Dotenv dotenv = null;
      String loadedFrom = null;

      // Try current directory first (when running from project root)
      try {
        dotenv = Dotenv.configure().directory("./").load();
        loadedFrom = "./";
      } catch (DotenvException e) {
        // Try parent directory (when running from blog-backend module)
        try {
          dotenv = Dotenv.configure().directory("../").load();
          loadedFrom = "../";
        } catch (DotenvException ex) {
          // Not found in either location
          System.out.println(
              "⚠ No .env file found in ./ or ../, using system environment variables and defaults");
          return;
        }
      }

      // Convert dotenv entries to a Map
      Map<String, Object> envMap = new HashMap<>();
      dotenv
          .entries()
          .forEach(
              entry -> {
                envMap.put(entry.getKey(), entry.getValue());
              });

      // Add to Spring Environment with high priority
      ConfigurableEnvironment environment = applicationContext.getEnvironment();
      environment.getPropertySources().addFirst(new MapPropertySource("dotenvProperties", envMap));

      System.out.println(
          "✓ Loaded "
              + envMap.size()
              + " environment variables from .env file (location: "
              + loadedFrom
              + ")");

    } catch (Exception e) {
      System.out.println("⚠ Error loading .env file: " + e.getMessage());
    }
  }
}
