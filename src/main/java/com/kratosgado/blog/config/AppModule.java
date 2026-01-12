package com.kratosgado.blog.config;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.kratosgado.blog.dao.CategoryDAO;
import com.kratosgado.blog.dao.PostDAO;
import com.kratosgado.blog.dao.TagDAO;
import com.kratosgado.blog.dao.UserDAO;
import com.kratosgado.blog.dao.nosql.CommentMongoDAO;
import com.kratosgado.blog.dao.nosql.ReviewMongoDAO;
import com.kratosgado.blog.services.AuthService;
import com.kratosgado.blog.services.CategoryService;
import com.kratosgado.blog.services.CommentService;
import com.kratosgado.blog.services.PostService;
import com.kratosgado.blog.services.ReviewService;
import com.kratosgado.blog.services.TagService;
import com.kratosgado.blog.services.UploadService;
import com.kratosgado.blog.services.UserService;

/**
 * Guice module for configuring dependency injection bindings.
 * All DAOs and Services are configured as singletons for optimal performance.
 */
public class AppModule extends AbstractModule {

  @Override
  protected void configure() {
    // Bind DAOs as singletons
    bind(UserDAO.class).in(Singleton.class);
    bind(PostDAO.class).in(Singleton.class);
    bind(CommentMongoDAO.class).in(Singleton.class);
    bind(CategoryDAO.class).in(Singleton.class);
    bind(TagDAO.class).in(Singleton.class);
    bind(ReviewMongoDAO.class).in(Singleton.class);

    // Bind Services as singletons
    bind(AuthService.class).in(Singleton.class);
    bind(UserService.class).in(Singleton.class);
    bind(PostService.class).in(Singleton.class);
    bind(CommentService.class).in(Singleton.class);
    bind(CategoryService.class).in(Singleton.class);
    bind(TagService.class).in(Singleton.class);
    bind(UploadService.class).in(Singleton.class);
    bind(ReviewService.class).in(Singleton.class);
  }
}
