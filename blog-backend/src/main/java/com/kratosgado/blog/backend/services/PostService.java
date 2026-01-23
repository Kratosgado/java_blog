package com.kratosgado.blog.backend.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kratosgado.blog.backend.cache.CacheConfig.PostCache;
import com.kratosgado.blog.backend.dao.PostDAO;
import com.kratosgado.blog.backend.dao.TagDAO;
import com.kratosgado.blog.backend.dao.UserDAO;
import com.kratosgado.blog.backend.dao.CategoryDAO;
import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.utils.DtoMapper;
import com.kratosgado.blog.dtos.request.CreatePostRequest;
import com.kratosgado.blog.dtos.request.UpdatePostRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.PostResponse;
import com.kratosgado.blog.models.Category;
import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.models.Tag;
import com.kratosgado.blog.models.User;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PostService {
  private final PostDAO postDAO;
  private final TagDAO tagDAO;
  private final UserDAO userDAO;
  private final CategoryDAO categoryDAO;
  private final PostCache postCache;

  public PostService(PostDAO postDAO, TagDAO tagDAO, UserDAO userDAO, CategoryDAO categoryDAO, PostCache postCache) {
    this.postDAO = postDAO;
    this.tagDAO = tagDAO;
    this.userDAO = userDAO;
    this.categoryDAO = categoryDAO;
    this.postCache = postCache;
  }

  public PostResponse createPost(CreatePostRequest request, User user) {
    Post post = new Post();
    post.setUserId(user.getId());
    post.setTitle(request.title());
    post.setSlug(generateSlug(request.title()));
    post.setContent(request.content());
    post.setExcerpt(request.excerpt());
    post.setCategoryId(request.categoryId() != null ? request.categoryId().intValue() : null);
    post.setCoverImage(request.coverImage());
    post.setStatus(request.status());

    Post savedPost = postDAO.createPost(post)
        .orElseThrow(() -> BlogException.internal("Failed to create post"));

    // Fetch tags for this post
    List<Tag> tags = tagDAO.getTagsByPostId(savedPost.getId());

    // Fetch related entities for response
    Category category = savedPost.getCategoryId() != null
        ? categoryDAO.getCategoryById(savedPost.getCategoryId()).orElse(null)
        : null;

    PostResponse response = DtoMapper.toPostResponse(savedPost, user, category, tags);
    return response;
  }

  public PostResponse updatePost(Long postId, UpdatePostRequest request, Long userId) {
    Post post = postDAO.getPostById(postId.intValue())
        .orElseThrow(() -> BlogException.notFound("Post not found"));

    if (!post.getUserId().equals(userId)) {
      throw BlogException.forbidden("You don't have permission to update this post");
    }

    if (request.title() != null) {
      post.setTitle(request.title());
      post.setSlug(generateSlug(request.title()));
    }
    if (request.content() != null)
      post.setContent(request.content());
    if (request.excerpt() != null)
      post.setExcerpt(request.excerpt());
    if (request.categoryId() != null)
      post.setCategoryId(request.categoryId().intValue());
    if (request.coverImage() != null)
      post.setCoverImage(request.coverImage());
    if (request.status() != null)
      post.setStatus(request.status().name());

    Post updatedPost = postDAO.updatePost(post)
        .orElseThrow(() -> BlogException.internal("Failed to update post"));

    // Fetch tags and related entities
    List<Tag> tags = tagDAO.getTagsByPostId(updatedPost.getId());
    User author = userDAO.getUserById(Long.valueOf(updatedPost.getUserId())).orElse(null);
    Category category = updatedPost.getCategoryId() != null
        ? categoryDAO.getCategoryById(updatedPost.getCategoryId()).orElse(null)
        : null;

    PostResponse response = DtoMapper.toPostResponse(updatedPost, author, category, tags);

    return response;
  }

  public void deletePost(Long postId, Long userId) {
    Post post = postDAO.getPostById(postId.intValue())
        .orElseThrow(() -> BlogException.notFound("Post not found"));

    if (!post.getUserId().equals(userId.intValue())) {
      throw BlogException.forbidden("You don't have permission to delete this post");
    }

    if (!postDAO.deletePost(postId.intValue())) {
      throw BlogException.internal("Failed to delete post");
    }
  }

  @Transactional
  public PostResponse getPostBySlug(String slug) {
    var postResponse = postCache.get(slug).orElseGet(() -> {
      log.debug("Cache miss for post slug: {}, fetching from database", slug);

      Post post = postDAO.getPostBySlug(slug)
          .orElseThrow(() -> BlogException.notFound("Post not found"));
      List<Tag> tags = tagDAO.getTagsByPostId(post.getId());
      PostResponse response = DtoMapper.toPostResponse(post, tags);

      // Cache the result
      postCache.put(post.getSlug(), response);

      return response;
    });
    postDAO.incrementViews(postResponse.id());
    return postResponse;
  }

  @Transactional(readOnly = true)
  public PostResponse getPostById(Long postId) {
    Post post = postDAO.getPostById(postId.intValue())
        .orElseThrow(() -> BlogException.notFound("Post not found"));

    List<Tag> tags = tagDAO.getTagsByPostId(post.getId());
    return DtoMapper.toPostResponse(post, tags);
  }

  @Transactional(readOnly = true)
  public PageResponse<PostResponse> getPublishedPosts(int page, int size) {
    List<Post> posts = postDAO.getPostsPaginated(page, size);
    int totalElements = postDAO.getPublishedPostCount();

    List<PostResponse> responses = posts.stream()
        .map(post -> {
          List<Tag> tags = tagDAO.getTagsByPostId(post.getId());
          return DtoMapper.toPostResponse(post, tags);
        })
        .collect(Collectors.toList());

    return DtoMapper.toPageResponse(responses, page, size, totalElements);
  }

  public PageResponse<PostResponse> searchPosts(String keyword, int page, int size) {
    List<Post> posts = postDAO.searchPostsByKeyword(keyword);

    // Manual pagination
    int startIndex = (page - 1) * size;
    int endIndex = Math.min(startIndex + size, posts.size());
    List<Post> paginatedPosts = posts.subList(Math.max(0, startIndex), Math.max(0, endIndex));

    List<PostResponse> responses = paginatedPosts.stream()
        .map(post -> {
          List<Tag> tags = tagDAO.getTagsByPostId(post.getId());
          return DtoMapper.toPostResponse(post, tags);
        })
        .collect(Collectors.toList());

    return DtoMapper.toPageResponse(responses, page, size, posts.size());
  }

  public PageResponse<PostResponse> getUserPosts(Long userId, int page, int size) {
    List<Post> posts = postDAO.getPostsByUserIdPaginated(userId.intValue(), page, size);
    List<Post> allUserPosts = postDAO.getPostsByUserId(userId.intValue());

    List<PostResponse> responses = posts.stream()
        .map(post -> {
          List<Tag> tags = tagDAO.getTagsByPostId(post.getId());
          return DtoMapper.toPostResponse(post, tags);
        })
        .collect(Collectors.toList());

    return DtoMapper.toPageResponse(responses, page, size, allUserPosts.size());
  }

  public PageResponse<PostResponse> getPostsByCategory(Long categoryId, int page, int size) {
    List<Post> posts = postDAO.getPostsByCategoryId(categoryId.intValue());

    // Manual pagination
    int startIndex = (page - 1) * size;
    int endIndex = Math.min(startIndex + size, posts.size());
    List<Post> paginatedPosts = posts.subList(Math.max(0, startIndex), Math.max(0, endIndex));

    List<PostResponse> responses = paginatedPosts.stream()
        .map(post -> {
          List<Tag> tags = tagDAO.getTagsByPostId(post.getId());
          return DtoMapper.toPostResponse(post, tags);
        })
        .collect(Collectors.toList());

    return DtoMapper.toPageResponse(responses, page, size, posts.size());
  }

  private String generateSlug(String title) {
    return title.toLowerCase()
        .replaceAll("[^a-z0-9\\s-]", "")
        .replaceAll("\\s+", "-")
        .replaceAll("-+", "-")
        .replaceAll("^-|-$", "");
  }

}
