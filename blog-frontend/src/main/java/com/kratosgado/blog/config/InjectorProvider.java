package com.kratosgado.blog.config;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Singleton provider for the Guice injector.
 * This class ensures a single Injector instance is available throughout the application.
 */
public class InjectorProvider {
  
  private static Injector injector;
  
  private InjectorProvider() {
    // Private constructor to prevent instantiation
  }
  
  /**
   * Initializes the injector with the application module.
   * Should be called once during application startup.
   */
  public static void initialize() {
    if (injector == null) {
      injector = Guice.createInjector(new AppModule());
    }
  }
  
  /**
   * Gets the singleton injector instance.
   * @return the Guice injector
   * @throws IllegalStateException if injector has not been initialized
   */
  public static Injector getInjector() {
    if (injector == null) {
      throw new IllegalStateException("Injector not initialized. Call initialize() first.");
    }
    return injector;
  }
  
  /**
   * Gets an instance of the specified class from the injector.
   * @param clazz the class to get an instance of
   * @param <T> the type parameter
   * @return an instance of the specified class
   */
  public static <T> T getInstance(Class<T> clazz) {
    return getInjector().getInstance(clazz);
  }
}
