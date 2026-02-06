package com.kratosgado.blog.backend.services;

import com.kratosgado.blog.backend.exceptions.ForbiddenException;
import com.kratosgado.blog.backend.exceptions.InvalidRequestException;
import com.kratosgado.blog.backend.exceptions.ResourceNotFoundException;
import com.kratosgado.blog.backend.repositories.jpa.CategoryRepository;
import com.kratosgado.blog.backend.repositories.jpa.PostRepository;
import com.kratosgado.blog.backend.repositories.jpa.TagRepository;
import com.kratosgado.blog.backend.utils.BlogConstants.CacheNames;
import com.kratosgado.blog.backend.utils.BlogUtils;
import com.kratosgado.blog.backend.utils.DtoMapper;
import com.kratosgado.blog.dtos.request.CreatePostRequest;
import com.kratosgado.blog.dtos.request.PageRequest;
import com.kratosgado.blog.dtos.request.UpdatePostRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.PostResponse.PostDetails;
import com.kratosgado.blog.dtos.response.PostResponse.PostView;
import com.kratosgado.blog.dtos.response.PostResponse.PostWithoutCategory;
import com.kratosgado.blog.dtos.response.PostResponse.PostWithoutUser;
import com.kratosgado.blog.enums.PostStatus;
import com.kratosgado.blog.models.Post;
import com.kratosgado.blog.models.Tag;
import com.kratosgado.blog.models.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
public class PostService {
  private final PostRepository postRepository;
  private final TagRepository tagRepository;
  private final CategoryRepository categoryRepository;

  @Transactional(isolation = Isolation.READ_COMMITTED)
  @CacheEvict(value = CacheNames.POSTLIST, allEntries = true)
  public PostDetails createPost(CreatePostRequest request, User user) {
    Post post = new Post();
    post.setUser(user);
    post.setTitle(request.title());
    post.setSlug(BlogUtils.toSlug(request.title()));
    post.setContent(request.content());
    post.setExcerpt(request.excerpt());

    if (request.categoryId() != null) {
      post.setCategory(
          categoryRepository
              .findById(request.categoryId())
              .orElseThrow(() -> new InvalidRequestException("Category not found")));
    }

    post.setCoverImage(request.coverImage());
    post.setStatus(PostStatus.valueOf(request.status().toLowerCase()));

    if (request.tagIds() != null && request.tagIds().length > 0) {
      List<Tag> tags = tagRepository.findAllById(List.of(request.tagIds()));
      post.setTags(tags);
    }

    return (PostDetails) postRepository.save(post);
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  @Caching(
      put = @CachePut(value = CacheNames.POSTS, key = "#result.slug"),
      evict = @CacheEvict(value = CacheNames.POSTLIST, allEntries = true))
  public PostDetails updatePost(Long postId, UpdatePostRequest request, Long userId) {
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

    if (!post.getUser().getId().equals(userId)) {
      throw new ForbiddenException("You don't have permission to update this post");
    }
    String newSlug = BlogUtils.toSlug(request.title());

    if (request.title() != null) {
      post.setTitle(request.title());
      post.setSlug(newSlug);
    }
    if (request.content() != null) post.setContent(request.content());
    if (request.excerpt() != null) post.setExcerpt(request.excerpt());
    if (request.categoryId() != null) {
      post.setCategory(
          categoryRepository
              .findById(request.categoryId())
              .orElseThrow(() -> new InvalidRequestException("Category not found")));
    }
    if (request.coverImage() != null) post.setCoverImage(request.coverImage());
    if (request.status() != null) post.setStatus(request.status());

    if (request.tagIds() != null) {
      List<Tag> tags = tagRepository.findAllById(List.of(request.tagIds()));
      post.setTags(tags);
    }

    return (PostDetails) postRepository.save(post);
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  @Caching(
      evict = {
        @CacheEvict(value = CacheNames.POSTLIST, allEntries = true),
        @CacheEvict(value = CacheNames.POSTS, key = "#post.slug")
      })
  public void deletePost(Long postId, Long userId) {
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

    if (!post.getUser().getId().equals(userId)) {
      throw new ForbiddenException("You don't have permission to delete this post");
    }

    postRepository.delete(post);
  }

  @Cacheable(value = CacheNames.POSTS, key = "#slug")
  public PostDetails getPostBySlug(String slug) {
    return postRepository
        .findBySlug(slug)
        .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
  }

  public PostDetails getPostById(Long postId) {
    return postRepository
        .findPostDetailsById(postId)
        .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  @CacheEvict(value = CacheNames.POSTLIST, allEntries = true)
  public PostDetails publishPost(Long postId, Long userId) {
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

    if (!post.getUser().getId().equals(userId)) {
      throw new ForbiddenException("You don't have permission to publish this post");
    }

    post.setStatus(PostStatus.published);
    return (PostDetails) postRepository.save(post);
  }

  @Cacheable(value = CacheNames.POSTLIST)
  public PageResponse<PostView> getPublishedPosts(PageRequest pageRequest) {
    var postsPage = postRepository.findByStatus(PostStatus.published, pageRequest.toPageable());
    return DtoMapper.toPageResponse(postsPage);
  }

  @Cacheable(value = CacheNames.POSTLIST)
  public PageResponse<PostView> searchPosts(String keyword, PageRequest pageRequest) {
    // Use optimized full-text search with PostgreSQL tsvector
    String wildcardQuery = "%" + keyword + "%";
    var postsPage =
        postRepository.searchPublishedPosts(
            wildcardQuery, keyword, pageRequest.toPageable());
    return DtoMapper.toPageResponse(postsPage);
  }

  @Cacheable(value = CacheNames.POSTLIST)
  public PageResponse<PostWithoutUser> getUserPosts(Long userId, PageRequest pageRequest) {
    var postsPage = postRepository.findByUserId(userId, pageRequest.toPageable());
    return DtoMapper.toPageResponse(postsPage);
  }

  @Cacheable(value = CacheNames.POSTLIST)
  public PageResponse<PostWithoutCategory> getPostsByCategory(
      Long categoryId, PageRequest pageRequest) {
    var postsPage = postRepository.findByCategoryId(categoryId, pageRequest.toPageable());
    return DtoMapper.toPageResponse(postsPage);
  }
}
