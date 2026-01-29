package com.kratosgado.blog.backend.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kratosgado.blog.backend.cache.CacheConfig.PostCache;
import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.jdbc.CategoryRepository;
import com.kratosgado.blog.backend.repositories.jdbc.PostRepository;
import com.kratosgado.blog.backend.repositories.jdbc.TagRepository;
import com.kratosgado.blog.backend.utils.BlogUtils;
import com.kratosgado.blog.backend.utils.DtoMapper;
import com.kratosgado.blog.dtos.request.CreatePostRequest;
import com.kratosgado.blog.dtos.request.PageRequest;
import com.kratosgado.blog.dtos.request.UpdatePostRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.PostResponse;
import com.kratosgado.blog.enums.PostStatus;
import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.models.User;

@Service
public class PostService {
  private final PostRepository postRepository;
  private final TagRepository tagRepository;
  private final CategoryRepository categoryRepository;
  private final PostCache postCache;

  public PostService(PostRepository postRepository, TagRepository tagRepository, CategoryRepository categoryRepository,
      PostCache postCache) {
    this.postRepository = postRepository;
    this.tagRepository = tagRepository;
    this.categoryRepository = categoryRepository;
    this.postCache = postCache;
  }

  public PostResponse createPost(CreatePostRequest request, User user) {
    Post post = new Post();
    post.setUserId(user.getId());
    post.setUser(user);
    post.setTitle(request.title());
    post.setSlug(BlogUtils.toSlug(request.title()));
    post.setContent(request.content());
    post.setExcerpt(request.excerpt());
    post.setCategoryId(request.categoryId());
    if (request.categoryId() != null
        && categoryRepository.findById(request.categoryId()).isEmpty()) {
      throw BlogException.badRequest("Category not found");
    }
    post.setCoverImage(request.coverImage());
    post.setStatus(PostStatus.valueOf(request.status().toLowerCase()));

    Post savedPost = postRepository.save(post);
    if (request.tagIds() != null && request.tagIds().length > 0) {
      tagRepository.savePostTags(savedPost.getId(), request.tagIds());
    }
    return DtoMapper.toPostResponse(savedPost);
  }

  public PostResponse updatePost(Long postId, UpdatePostRequest request, Long userId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> BlogException.notFound("Post not found"));

    if (!post.getUserId().equals(userId)) {
      throw BlogException.forbidden("You don't have permission to update this post");
    }
    post.onUpdate();

    if (request.title() != null) {
      post.setTitle(request.title());
      post.setSlug(BlogUtils.toSlug(request.title()));
    }
    if (request.content() != null)
      post.setContent(request.content());
    if (request.excerpt() != null)
      post.setExcerpt(request.excerpt());
    if (request.categoryId() != null) {
      post.setCategoryId(request.categoryId());
      categoryRepository.findById(request.categoryId())
          .ifPresent(post::setCategory);
    }
    if (request.coverImage() != null)
      post.setCoverImage(request.coverImage());
    if (request.status() != null)
      post.setStatus(request.status());

    if (request.tagIds() != null) {
      tagRepository.deletePostTags(postId);
      tagRepository.savePostTags(postId, request.tagIds());
    }

    Post updatedPost = postRepository.update(post);
    var response = DtoMapper.toPostResponse(updatedPost);
    postCache.updateIfPresent(response.slug(), response);
    return response;
  }

  public void deletePost(Long postId, Long userId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> BlogException.notFound("Post not found"));

    if (!post.getUserId().equals(userId)) {
      throw BlogException.forbidden("You don't have permission to delete this post");
    }

    postRepository.deleteById(postId);
  }

  @Transactional
  public PostResponse getPostBySlug(String slug) {
    return postCache.get(slug).orElseGet(() -> {
      Post post = postRepository.findBySlug(slug)
          .orElseThrow(() -> BlogException.notFound("Post not found"));
      PostResponse response = DtoMapper.toPostResponse(post);
      postCache.put(post.getSlug(), response);
      return response;
    });
  }

  public PostResponse getPostById(Long postId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> BlogException.notFound("Post not found"));
    return DtoMapper.toPostResponse(post);

  }

  public PostResponse publishPost(Long postId, Long userId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> BlogException.notFound("Post not found"));

    if (!post.getUserId().equals(userId)) {
      throw BlogException.forbidden("You don't have permission to publish this post");
    }

    post.setStatus(PostStatus.published);
    post.onUpdate();
    Post updatedPost = postRepository.update(post);
    return DtoMapper.toPostResponse(updatedPost);
  }

  public PageResponse<PostResponse> getPublishedPosts(PageRequest pageRequest) {
    int offset = pageRequest.getOffset();
    List<Post> posts = postRepository.findPublishedPosts(pageRequest.getSize(), offset, pageRequest.getSortBy(),
        pageRequest.getSortDir());
    List<PostResponse> postResponses = posts.stream().map(DtoMapper::toPostResponse).toList();
    long totalElements;
    try {
      totalElements = postRepository.countPublishedPosts();
    } catch (Exception e) {
      totalElements = 0;
    }
    return DtoMapper.toPageResponse(postResponses, pageRequest.getPage(), pageRequest.getSize(), (int) totalElements);
  }

  public PageResponse<PostResponse> searchPosts(String keyword, PageRequest pageRequest) {
    int offset = pageRequest.getOffset();
    List<Post> posts = postRepository.searchPostsByKeyword(keyword, pageRequest.getSize(), offset,
        pageRequest.getSortBy(), pageRequest.getSortDir());
    List<PostResponse> postResponses = posts.stream().map(DtoMapper::toPostResponse).toList();
    long totalElements = postRepository.countPostsByKeyword(keyword);
    return DtoMapper.toPageResponse(postResponses, pageRequest.getPage(), pageRequest.getSize(), (int) totalElements);
  }

  public PageResponse<PostResponse> getUserPosts(Long userId, PageRequest pageRequest) {
    int offset = pageRequest.getOffset();
    List<Post> posts = postRepository.findPostsByUser(userId, pageRequest.getSize(), offset, pageRequest.getSortBy(),
        pageRequest.getSortDir());
    List<PostResponse> postResponses = posts.stream().map(DtoMapper::toPostResponse).toList();
    long totalElements = postRepository.countPostsByUser(userId);
    return DtoMapper.toPageResponse(postResponses, pageRequest.getPage(), pageRequest.getSize(), (int) totalElements);
  }

  public PageResponse<PostResponse> getPostsByCategory(Long categoryId, PageRequest pageRequest) {
    int offset = pageRequest.getOffset();
    List<Post> posts = postRepository.findPostsByCategory(categoryId, pageRequest.getSize(), offset,
        pageRequest.getSortBy(), pageRequest.getSortDir());
    List<PostResponse> postResponses = posts.stream().map(DtoMapper::toPostResponse).toList();
    long totalElements = postRepository.countPostsByCategory(categoryId);
    return DtoMapper.toPageResponse(postResponses, pageRequest.getPage(), pageRequest.getSize(), (int) totalElements);
  }
}
