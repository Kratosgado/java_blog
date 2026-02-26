package com.kratosgado.blog.backend.utils;

public class BlogConstants {
  public static class Miliseconds {
    public static final long ONE_MINUTE = 60 * 1000;
    public static final long FIVE_MINUTES = 5 * ONE_MINUTE;
    public static final long FIFTEEN_MINUTES = 15 * ONE_MINUTE;
    public static final long THIRTY_MINUTES = 30 * ONE_MINUTE;
    public static final long ONE_HOUR = 60 * ONE_MINUTE;
    public static final long SIX_HOURS = 6 * ONE_HOUR;
    public static final long TWELVE_HOURS = 12 * ONE_HOUR;
    public static final long ONE_DAY = 24 * ONE_HOUR;
  }

  public static class CacheNames {
    public static final String POSTS = "posts";
    public static final String POSTLIST = "postList";
    public static final String TAGS = "tags";
    public static final String TAGLIST = "tagList";
    public static final String COMMENTS = "comments";
    public static final String REVIEWLIST = "reviewList";
    public static final String REVIEWS = "reviews";
    public static final String COMMENTLIST = "commentList";
    public static final String DASHBOARDSTATS = "dashboardStats";
  }

  public static class ExecutorNames {
    public static final String taskExecutor = "taskExecutor";
    public static final String notificationExecutor = "notificationExecutor";
  }
}
