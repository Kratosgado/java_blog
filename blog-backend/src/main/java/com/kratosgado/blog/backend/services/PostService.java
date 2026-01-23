package com.kratosgado.blog.backend.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kratosgado.blog.backend.cache.CacheConfig.PostCache;
import com.kratosgado.blog.backend.exceptions.BlogException;
import com.kratosgado.blog.backend.repositories.jpa.CategoryRepository;
import com.kratosgado.blog.backend.repositories.jpa.PostRepository;
import com.kratosgado.blog.backend.repositories.jpa.TagRepository;
import com.kratosgado.blog.backend.repositories.jpa.UserRepository;
import com.kratosgado.blog.backend.utils.DtoMapper;
import com.kratosgado.blog.dtos.request.CreatePostRequest;
import com.kratosgado.blog.dtos.request.UpdatePostRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.PostResponse;
import com.kratosgado.blog.enums.PostStatus;
import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.models.Tag;
import com.kratosgado.blog.models.User;

@Service
public class PostService {
  private final PostRepository postRepository;
  private final TagRepository tagRepository;
  private final UserRepository userRepository;
  private final CategoryRepository categoryRepository;
  private final PostCache postCache;

  public PostService(PostRepository postRepository, TagRepository tagRepository, UserRepository userRepository,
      CategoryRepository categoryRepository, PostCache postCache) {
    this.postRepository = postRepository;
    this.tagRepository = tagRepository;
    this.userRepository = userRepository;
    this.categoryRepository = categoryRepository;
    this.postCache = postCache;
  }

  public PostResponse createPost(CreatePostRequest request, User user) {
    Post post = new Post();
    post.setUserId(user.getId());
    post.setTitle(request.title());
    post.setSlug(generateSlug(request.title()));
    post.setContent(request.content());
    post.setExcerpt(request.excerpt());
    post.setCategoryId(request.categoryId());
    post.setCoverImage(request.coverImage());
    post.setStatus(PostStatus.valueOf(request.status()));

    Post savedPost = postRepository.save(post);
    return DtoMapper.toPostResponse(savedPost);
  }

  public PostResponse updatePost(Long postId, UpdatePostRequest request, Long userId) {
    Post post = postRepository.findById(postId)
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
      post.setCategoryId(request.categoryId());
    if (request.coverImage() != null)
      post.setCoverImage(request.coverImage());
    if (request.status() != null)
      post.setStatus(request.status());

    Post updatedPost = postRepository.save(post);
    return DtoMapper.toPostResponse(updatedPost);

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
      List<Tag> tags = tagRepository.findByPostId(post.getId());
      PostResponse response = DtoMapper.toPostResponse(post);
      postCache.put(post.getSlug(), response);
      return response;
    });
  }

  @Transactional(readOnly = true)
  public PostResponse getPostById(Long postId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> BlogException.notFound("Post not found"));

    return DtoMapper.toPostResponse(post);
  }

  @Transactional(readOnly = true)
  public PageResponse<PostResponse> getPublishedPosts(Pageable pageable) {
    Page<Post> postPage = postRepository.findPublishedPosts(pageable);
    Page<PostResponse> responsePage = new PageImpl<>(postPage.map(DtoMapper::toPostResponse).toList());
    return DtoMapper.toPageResponse(responsePage, pageable);
  }

  @Transactional(readOnly = true)
  public PageResponse<PostResponse> getPublishedPosts(int page, int size) {
    return getPublishedPosts(PageRequest.of(page - 1, size));
  }

  public PageResponse<PostResponse> searchPosts(String keyword, Pageable pageable) {
    Page<Post> postPage = postRepository.searchPublishedPosts(keyword, pageable);
    var responsePage = new PageImpl<>(postPage.map(DtoMapper::toPostResponse).toList());
    return DtoMapper.toPageResponse(responsePage, pageable);
  }

  public PageResponse<PostResponse> searchPosts(String keyword, int page, int size) {
    return searchPosts(keyword, PageRequest.of(page - 1, size));
  }

  public PageResponse<PostResponse> getUserPosts(Long userId, Pageable pageable) {
    Page<Post> postPage = postRepository.findByUserId(userId, pageable);
    var responsePage = new PageImpl<>(postPage.map(DtoMapper::toPostResponse).toList());
    return DtoMapper.toPageResponse(responsePage, pageable);
  }

  public PageResponse<PostResponse> getUserPosts(Long userId, int page, int size) {
    return getUserPosts(userId, PageRequest.of(page - 1, size));
  }

  public PageResponse<PostResponse> getPostsByCategory(Long categoryId, Pageable pageable) {
    Page<Post> postPage = postRepository.findByCategoryId(categoryId, pageable);
    var responsePage = new PageImpl<>(postPage.map(DtoMapper::toPostResponse).toList());
    return DtoMapper.toPageResponse(responsePage, pageable);
  }

  public PageResponse<PostResponse> getPostsByCategory(Long categoryId, int page, int size) {
    return getPostsByCategory(categoryId, PageRequest.of(page - 1, size));
  }

  private String generateSlug(String title) {
    return title.toLowerCase()
        .replaceAll("[^a-z0-9\\s-]", "")
        .replaceAll("\\s+", "-")
        .replaceAll("-+", "-")
        .replaceAll("^-|-$", "");
  }

}
