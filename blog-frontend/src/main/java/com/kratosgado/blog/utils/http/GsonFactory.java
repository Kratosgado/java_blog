package com.kratosgado.blog.utils.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GsonFactory {
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

  private static Gson instance;

  /**
   * Get a singleton Gson instance configured for the application
   */
  public static synchronized Gson getGson() {
    if (instance == null) {
      instance = createGson();
    }
    return instance;
  }

  /**
   * Create a new Gson instance with custom type adapters
   */
  private static Gson createGson() {
    return new GsonBuilder()
        // LocalDateTime serializer and deserializer
        .registerTypeAdapter(LocalDateTime.class,
            (JsonSerializer<LocalDateTime>) (src, typeOfSrc,
                context) -> new JsonPrimitive(src.format(DATE_TIME_FORMATTER)))
        .registerTypeAdapter(LocalDateTime.class,
            (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> LocalDateTime.parse(json.getAsString(),
                DATE_TIME_FORMATTER))
        // LocalDate serializer and deserializer
        .registerTypeAdapter(LocalDate.class,
            (JsonSerializer<LocalDate>) (src, typeOfSrc, context) -> new JsonPrimitive(src.format(DATE_FORMATTER)))
        .registerTypeAdapter(LocalDate.class,
            (JsonDeserializer<LocalDate>) (json, typeOfT, context) -> LocalDate.parse(json.getAsString(),
                DATE_FORMATTER))
        .create();
  }
}
