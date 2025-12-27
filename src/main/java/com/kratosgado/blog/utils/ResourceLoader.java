package com.kratosgado.blog.utils;

import java.io.InputStream;
import java.net.URL;

public class ResourceLoader {

  private ResourceLoader() {
  }

  public static URL loadURL(String path) {
    return ResourceLoader.class.getResource(path);
  }

  public static String load(String path) {
    return loadURL(path).toString();
  }

  public static String loadCss(String name) {
    return load("/css/" + name + ".css");
  }

  public static InputStream loadStream(String name) {
    return ResourceLoader.class.getResourceAsStream(name);
  }

}
