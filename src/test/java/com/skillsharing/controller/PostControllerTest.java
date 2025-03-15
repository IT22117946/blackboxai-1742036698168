package com.skillsharing.controller;

import com.skillsharing.BaseTest;
import com.skillsharing.model.Post;
import com.skillsharing.service.PostService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PostControllerTest extends BaseTest {

    @MockBean
    private PostService postService;

    @Test
    void createPost_ShouldCreateAndReturnPost() throws Exception {
        // Arrange
        Post post = createTestPost();
        when(postService.createPost(any(Post.class), eq(testUser.getId()))).thenReturn(post);

        // Act & Assert
        mockMvc.perform(post("/api/posts")
                .header("Authorization", getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(post)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(post.getId()))
                .andExpect(jsonPath("$.description").value(post.getDescription()));
    }

    @Test
    void getPost_ShouldReturnPost() throws Exception {
        // Arrange
        Post post = createTestPost();
        when(postService.getPostById(post.getId())).thenReturn(post);

        // Act & Assert
        mockMvc.perform(get("/api/posts/{id}", post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(post.getId()))
                .andExpect(jsonPath("$.description").value(post.getDescription()));
    }

    @Test
    void updatePost_ShouldUpdateAndReturnPost() throws Exception {
        // Arrange
        Post post = createTestPost();
        post.setDescription("Updated description");
        when(postService.updatePost(eq(post.getId()), any(Post.class), eq(testUser.getId())))
                .thenReturn(post);

        // Act & Assert
        mockMvc.perform(put("/api/posts/{id}", post.getId())
                .header("Authorization", getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(post)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    void deletePost_ShouldDeleteSuccessfully() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/posts/{id}", 1L)
                .header("Authorization", getAuthHeader()))
                .andExpect(status().isOk());
    }

    @Test
    void likePost_ShouldLikeSuccessfully() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/posts/{id}/like", 1L)
                .header("Authorization", getAuthHeader()))
                .andExpect(status().isOk());
    }

    @Test
    void unlikePost_ShouldUnlikeSuccessfully() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/posts/{id}/unlike", 1L)
                .header("Authorization", getAuthHeader()))
                .andExpect(status().isOk());
    }

    @Test
    void getUserPosts_ShouldReturnUserPosts() throws Exception {
        // Arrange
        List<Post> posts = Arrays.asList(createTestPost(), createTestPost());
        Page<Post> postPage = new PageImpl<>(posts);
        when(postService.getUserPosts(eq(testUser.getId()), any(Pageable.class))).thenReturn(postPage);

        // Act & Assert
        mockMvc.perform(get("/api/posts/user/{userId}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void getFeed_ShouldReturnFeed() throws Exception {
        // Arrange
        List<Post> posts = Arrays.asList(createTestPost(), createTestPost());
        Page<Post> postPage = new PageImpl<>(posts);
        when(postService.getFollowingPosts(eq(testUser.getId()), any(Pageable.class))).thenReturn(postPage);

        // Act & Assert
        mockMvc.perform(get("/api/posts/feed")
                .header("Authorization", getAuthHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void searchPosts_ShouldReturnMatchingPosts() throws Exception {
        // Arrange
        List<Post> posts = Arrays.asList(createTestPost(), createTestPost());
        Page<Post> postPage = new PageImpl<>(posts);
        when(postService.searchPosts(eq("test"), any(Pageable.class))).thenReturn(postPage);

        // Act & Assert
        mockMvc.perform(get("/api/posts/search")
                .param("keyword", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    private Post createTestPost() {
        Post post = new Post();
        post.setId(1L);
        post.setUser(testUser);
        post.setDescription("Test post description");
        post.setPostType(Post.PostType.SKILL_SHARING);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        return post;
    }
}
