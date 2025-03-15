package com.skillsharing.controller;

import com.skillsharing.BaseTest;
import com.skillsharing.model.Comment;
import com.skillsharing.model.Post;
import com.skillsharing.service.CommentService;
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

class CommentControllerTest extends BaseTest {

    @MockBean
    private CommentService commentService;

    @Test
    void createComment_ShouldCreateAndReturnComment() throws Exception {
        // Arrange
        Comment comment = createTestComment();
        String content = "Test comment content";
        when(commentService.createComment(eq(1L), eq(testUser.getId()), eq(content)))
                .thenReturn(comment);

        // Act & Assert
        mockMvc.perform(post("/api/posts/{postId}/comments", 1L)
                .header("Authorization", getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(comment.getId()))
                .andExpect(jsonPath("$.content").value(comment.getContent()));
    }

    @Test
    void getPostComments_ShouldReturnComments() throws Exception {
        // Arrange
        List<Comment> comments = Arrays.asList(createTestComment(), createTestComment());
        Page<Comment> commentPage = new PageImpl<>(comments);
        when(commentService.getPostComments(eq(1L), any(Pageable.class))).thenReturn(commentPage);

        // Act & Assert
        mockMvc.perform(get("/api/posts/{postId}/comments", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void getUserComments_ShouldReturnUserComments() throws Exception {
        // Arrange
        List<Comment> comments = Arrays.asList(createTestComment(), createTestComment());
        Page<Comment> commentPage = new PageImpl<>(comments);
        when(commentService.getUserComments(eq(testUser.getId()), any(Pageable.class)))
                .thenReturn(commentPage);

        // Act & Assert
        mockMvc.perform(get("/api/users/{userId}/comments", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void updateComment_ShouldUpdateAndReturnComment() throws Exception {
        // Arrange
        Comment comment = createTestComment();
        String updatedContent = "Updated comment content";
        comment.setContent(updatedContent);
        when(commentService.updateComment(eq(1L), eq(testUser.getId()), eq(updatedContent)))
                .thenReturn(comment);

        // Act & Assert
        mockMvc.perform(put("/api/comments/{id}", 1L)
                .header("Authorization", getAuthHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedContent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(updatedContent));
    }

    @Test
    void deleteComment_ShouldDeleteSuccessfully() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/comments/{id}", 1L)
                .header("Authorization", getAuthHeader()))
                .andExpect(status().isOk());
    }

    @Test
    void getCommentsCount_ShouldReturnCount() throws Exception {
        // Arrange
        when(commentService.getCommentsCount(1L)).thenReturn(5L);

        // Act & Assert
        mockMvc.perform(get("/api/posts/{postId}/comments/count", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    @Test
    void deleteAllPostComments_ShouldDeleteSuccessfully() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/posts/{postId}/comments", 1L)
                .header("Authorization", getAuthHeader()))
                .andExpect(status().isOk());
    }

    private Comment createTestComment() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setUser(testUser);
        comment.setPost(createTestPost());
        comment.setContent("Test comment content");
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        return comment;
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
