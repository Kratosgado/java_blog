package com.kratosgado.blog.backend.controllers.v2;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kratosgado.blog.backend.config.JpaConfig;
import com.kratosgado.blog.backend.config.VersionConfig;
import com.kratosgado.blog.backend.config.database.DataSourceConfig;
import com.kratosgado.blog.backend.security.JwtAuthenticationFilter;
import com.kratosgado.blog.backend.security.JwtUtil;
import com.kratosgado.blog.backend.services.PostService;
import com.kratosgado.blog.dtos.request.PageRequest;
import com.kratosgado.blog.dtos.request.SearchPageRequest;
import com.kratosgado.blog.dtos.response.PageResponse;
import com.kratosgado.blog.dtos.response.PostResponse.PostView;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = PostControllerV2.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {JpaConfig.class, DataSourceConfig.class}))
@Import(VersionConfig.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for simple unit tests
@DisplayName("PostController V2 Tests")
class PostControllerV2Test {

  @Autowired private MockMvc mockMvc;

  @MockBean private PostService postService;
  @MockBean private JwtUtil jwtUtil; // Required due to security context loading
  @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Test
  @DisplayName("Should return trending posts")
  void getTrendingPosts_ShouldReturnOk() throws Exception {
    PageResponse<PostView> pageResponse =
        new PageResponse<>(Collections.emptyList(), 0, 10, 0, 0, true, true);
    given(postService.getTrendingPosts(any(PageRequest.class))).willReturn(pageResponse);

    mockMvc
        .perform(get("/v2/posts/trending").param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content").isArray());
  }

  @Test
  @DisplayName("Should return optimized category posts")
  void getCategoryPostsOptimized_ShouldReturnOk() throws Exception {
    PageResponse<PostView> pageResponse =
        new PageResponse<>(Collections.emptyList(), 0, 10, 0, 0, true, true);
    given(postService.getPublishedPostsByCategoryOptimized(anyLong(), any(PageRequest.class)))
        .willReturn(pageResponse);

    mockMvc
        .perform(get("/v2/posts/category/{id}/optimized", 1L).param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content").isArray());
  }

  @Test
  @DisplayName("Should return optimized tag posts")
  void getTagPostsOptimized_ShouldReturnOk() throws Exception {
    PageResponse<PostView> pageResponse =
        new PageResponse<>(Collections.emptyList(), 0, 10, 0, 0, true, true);
    given(postService.getPublishedPostsByTagOptimized(anyLong(), any(PageRequest.class)))
        .willReturn(pageResponse);

    mockMvc
        .perform(get("/v2/posts/tag/{id}/optimized", 1L).param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content").isArray());
  }

  @Test
  @DisplayName("Should return optimized search results")
  void searchPosts_ShouldReturnOk() throws Exception {
    PageResponse<PostView> pageResponse =
        new PageResponse<>(Collections.emptyList(), 0, 10, 0, 0, true, true);
    given(postService.searchPosts(eq("test"), any(SearchPageRequest.class)))
        .willReturn(pageResponse);

    mockMvc
        .perform(
            get("/v2/posts/search").param("keyword", "test").param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content").isArray());
  }
}
