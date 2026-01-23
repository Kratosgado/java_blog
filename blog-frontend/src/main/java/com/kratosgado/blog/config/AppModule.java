package com.kratosgado.blog.config;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.kratosgado.blog.services.AuthService;
import com.kratosgado.blog.services.CategoryService;
import com.kratosgado.blog.services.CommentService;
import com.kratosgado.blog.services.DashboardService;
import com.kratosgado.blog.services.PostService;
import com.kratosgado.blog.services.ReviewService;
import com.kratosgado.blog.services.TagService;
import com.kratosgado.blog.services.UploadService;
import com.kratosgado.blog.services.UserService;

/**
 * Guice module for configuring dependency injection bindings.
 * All Services are configured as singletons for optimal performance.
 * Services now use API clients to communicate with the backend.
 */
public class AppModule extends AbstractModule {

  @Override
  protected void configure() {
    // Bind Services as singletons
    bind(AuthService.class).in(Singleton.class);
    bind(UserService.class).in(Singleton.class);
    bind(PostService.class).in(Singleton.class);
    bind(CommentService.class).in(Singleton.class);
    bind(CategoryService.class).in(Singleton.class);
    bind(TagService.class).in(Singleton.class);
    bind(UploadService.class).in(Singleton.class);
    bind(ReviewService.class).in(Singleton.class);
    bind(DashboardService.class).in(Singleton.class);
  }
}
