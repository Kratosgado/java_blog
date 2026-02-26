package com.kratosgado.blog.backend.services;

import com.kratosgado.blog.backend.exceptions.ForbiddenException;
import com.kratosgado.blog.backend.exceptions.InvalidRequestException;
import com.kratosgado.blog.backend.exceptions.ResourceNotFoundException;
import com.kratosgado.blog.backend.models.Post;
import com.kratosgado.blog.backend.models.Tag;
import com.kratosgado.blog.backend.models.User;
import com.kratosgado.blog.backend.repositories.jpa.CategoryRepository;
import com.kratosgado.blog.backend.repositories.jpa.PostRepository;
import com.kratosgado.blog.backend.repositories.jpa.TagRepository;
import com.kratosgado.blog.backend.utils.BlogConstants.CacheNames;
import com.kratosgado.blog.backend.utils.BlogConstants.Miliseconds;
import com.kratosgado.blog.backend.utils.BlogUtils;
import com.kratosgado.blog.backend.utils.DtoMapper;
import com.kratosgado.blog.backend.utils.PageUtil;
import com.kratosgado.blog.dtos.request.CreatePostRequest;
import com.kratosgado.blog.dtos.request.PageRequest;
import com.kratosgado.blog.dtos.request.SearchPageRequest;
import com.kratosgado.blog.dtos.request.UpdatePostRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.PostResponse.PostDetails;
import com.kratosgado.blog.dtos.response.PostResponse.PostView;
import com.kratosgado.blog.dtos.response.PostResponse.PostWithoutCategory;
import com.kratosgado.blog.dtos.response.PostResponse.PostWithoutUser;
import com.kratosgado.blog.enums.PostStatus;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
  private final PostRepository postRepository;
  private final TagRepository tagRepository;
  private final CategoryRepository categoryRepository;

  // In-memory cache for the first page of trending posts to avoid DB hits
  private final AtomicReference<PageResponse<PostView>> trendingPostsCache =
      new AtomicReference<>();

  @Scheduled(fixedRate = Miliseconds.SIX_HOURS)
  @Transactional(readOnly = true)
  @jakarta.annotation.PostConstruct
  public void refreshTrendingPostsCache() {
    PageRequest firstPage = new PageRequest(0, 20, "views", "desc");
    java.time.LocalDateTime sinceDate = java.time.LocalDateTime.now().minusDays(30);
    Page<PostView> postsPage =
        postRepository.findTrendingPosts(
            PostStatus.published, sinceDate, PageUtil.toPageable(firstPage));
    trendingPostsCache.set(DtoMapper.toPageResponse(postsPage));
  }

  public PageResponse<PostView> getTrendingPosts(PageRequest pageRequest) {
    // Serve from in-memory cache if it's the first page and cache is available
    if (pageRequest.getPage() == 0 && pageRequest.getSize() <= 20) {
      PageResponse<PostView> cached = trendingPostsCache.get();
      if (cached != null) {
        if (pageRequest.getSize() == 20) {
          return cached;
        } else {
          // Slice the cached list if a smaller size is requested
          List<PostView> slicedContent =
              cached.content().subList(0, Math.min(pageRequest.getSize(), cached.content().size()));
          int totalPages = (int) Math.ceil((double) cached.totalElements() / pageRequest.getSize());
          return new PageResponse<>(
              slicedContent,
              0, // currentPage
              pageRequest.getSize(), // pageSize
              cached.totalElements(), // totalElements
              totalPages, // totalPages
              true, // first
              totalPages <= 1 // last
              );
        }
      }
    }

    // Fallback to database query
    java.time.LocalDateTime sinceDate = java.time.LocalDateTime.now().minusDays(30);
    var postsPage =
        postRepository.findTrendingPosts(
            PostStatus.published, sinceDate, PageUtil.toPageable(pageRequest));
    return DtoMapper.toPageResponse(postsPage);
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  @CacheEvict(value = CacheNames.POSTLIST, allEntries = true)
  public Post createPost(CreatePostRequest request, User user) {
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

    return postRepository.save(post);
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  @Caching(
      put = @CachePut(value = CacheNames.POSTS, key = "#result.slug"),
      evict = @CacheEvict(value = CacheNames.POSTLIST, allEntries = true))
  public Post updatePost(Long postId, UpdatePostRequest request, Long userId) {
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

    return postRepository.save(post);
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  @Caching(
      evict = {
        @CacheEvict(value = CacheNames.POSTLIST, allEntries = true),
        @CacheEvict(value = CacheNames.POSTS, key = "#result.slug")
      })
  public Post deletePost(Long postId, Long userId) {
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
    if (!post.getUser().getId().equals(userId)) {
      throw new ForbiddenException("You don't have permission to delete this post");
    }

    postRepository.deleteById(postId);
    return post;
  }

  @Cacheable(value = CacheNames.POSTS, key = "#slug")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
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
  public Post publishPost(Long postId, Long userId) {
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

    if (!post.getUser().getId().equals(userId)) {
      throw new ForbiddenException("You don't have permission to publish this post");
    }

    post.setStatus(PostStatus.published);
    return postRepository.save(post);
  }

  @Cacheable(value = CacheNames.POSTLIST)
  public PageResponse<PostView> getPublishedPosts(PageRequest pageRequest) {
    var postsPage =
        postRepository.findByStatus(PostStatus.published, PageUtil.toPageable(pageRequest));
    return DtoMapper.toPageResponse(postsPage);
  }

  @Cacheable(value = CacheNames.POSTLIST)
  public PageResponse<PostView> searchPosts(SearchPageRequest pageRequest) {
    // Use optimized full-text search with PostgreSQL tsvector
    var postsPage =
        postRepository.searchPublishedPosts(
            pageRequest.getKeyword(), PageUtil.toPageable(pageRequest));
    return DtoMapper.toPageResponse(postsPage);
  }

  @Cacheable(value = CacheNames.POSTLIST)
  public PageResponse<PostView> searchPostsV1(SearchPageRequest pageRequest) {
    // Use optimized full-text search with PostgreSQL tsvector
    var postsPage =
        postRepository.searchPublishedPostsSimple(
            pageRequest.getKeyword(), PageUtil.toPageable((PageRequest) pageRequest));
    return DtoMapper.toPageResponse(postsPage);
  }

  @Cacheable(value = CacheNames.POSTLIST)
  public PageResponse<PostWithoutUser> getUserPosts(Long userId, PageRequest pageRequest) {
    var postsPage = postRepository.findByUserId(userId, PageUtil.toPageable(pageRequest));
    return DtoMapper.toPageResponse(postsPage);
  }

  @Cacheable(value = CacheNames.POSTLIST)
  public PageResponse<PostWithoutCategory> getPostsByCategory(
      Long categoryId, PageRequest pageRequest) {
    var postsPage = postRepository.findByCategoryId(categoryId, PageUtil.toPageable(pageRequest));
    return DtoMapper.toPageResponse(postsPage);
  }

  @Cacheable(
      value = CacheNames.POSTLIST,
      key = "'category-opt-' + #categoryId + '-' + #pageRequest.toString()")
  public PageResponse<PostView> getPublishedPostsByCategoryOptimized(
      Long categoryId, PageRequest pageRequest) {
    var postsPage =
        postRepository.findPublishedPostsByCategoryOptimized(
            categoryId, PageUtil.toPageable(pageRequest));
    return DtoMapper.toPageResponse(postsPage);
  }

  @Cacheable(
      value = CacheNames.POSTLIST,
      key = "'tag-opt-' + #tagId + '-' + #pageRequest.toString()")
  public PageResponse<PostView> getPublishedPostsByTagOptimized(
      Long tagId, PageRequest pageRequest) {
    var postsPage =
        postRepository.findPublishedPostsByTagOptimized(tagId, PageUtil.toPageable(pageRequest));
    return DtoMapper.toPageResponse(postsPage);
  }
}
