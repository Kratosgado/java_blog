package com.kratosgado.blog.backend.services;

import com.kratosgado.blog.backend.models.Post;
import com.kratosgado.blog.backend.models.User;
import com.kratosgado.blog.dtos.request.CreatePostRequest;
import com.kratosgado.blog.dtos.request.PageRequest;
import com.kratosgado.blog.dtos.request.SearchPageRequest;
import com.kratosgado.blog.dtos.request.UpdatePostRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.PostResponse.PostDetails;
import com.kratosgado.blog.dtos.response.PostResponse.PostView;
import com.kratosgado.blog.dtos.response.PostResponse.PostWithoutCategory;
import com.kratosgado.blog.dtos.response.PostResponse.PostWithoutUser;

public interface PostService {

  void refreshTrendingPostsCache();

  PageResponse<PostView> getTrendingPosts(PageRequest pageRequest);

  Post createPost(CreatePostRequest request, User user);

  Post updatePost(Long postId, UpdatePostRequest request, Long userId);

  Post deletePost(Long postId, Long userId);

  PostDetails getPostBySlug(String slug);

  PostDetails getPostById(Long postId);

  Post publishPost(Long postId, Long userId);

  PageResponse<PostView> getPublishedPosts(PageRequest pageRequest);

  PageResponse<PostView> searchPosts(SearchPageRequest pageRequest);

  PageResponse<PostView> searchPostsV1(SearchPageRequest pageRequest);

  PageResponse<PostWithoutUser> getUserPosts(Long userId, PageRequest pageRequest);

  PageResponse<PostWithoutCategory> getPostsByCategory(Long categoryId, PageRequest pageRequest);

  PageResponse<PostView> getPublishedPostsByCategoryOptimized(
      Long categoryId, PageRequest pageRequest);

  PageResponse<PostView> getPublishedPostsByTagOptimized(Long tagId, PageRequest pageRequest);
}
