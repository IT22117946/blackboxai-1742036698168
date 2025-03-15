package com.skillsharing.service;

import com.skillsharing.exception.ResourceNotFoundException;
import com.skillsharing.model.Comment;
import com.skillsharing.model.Post;
import com.skillsharing.model.User;
import com.skillsharing.repository.CommentRepository;
import com.skillsharing.repository.PostRepository;
import com.skillsharing.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private CommentService commentService;

    private User testUser;
    private Post testPost;
    private Comment testComment;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");

        testPost = new Post();
        testPost.setId(1L);
        testPost.setUser(testUser);
        testPost.setDescription("Test post description");

        testComment = new Comment();
        testComment.setId(1L);
        testComment.setUser(testUser);
        testComment.setPost(testPost);
        testComment.setContent("Test comment content");
        testComment.setCreatedAt(LocalDateTime.now());
        testComment.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void createComment_ValidComment_ShouldCreateSuccessfully() {
        // Arrange
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        // Act
        Comment result = commentService.createComment(1L, 1L, "Test comment content");

        // Assert
        assertNotNull(result);
        assertEquals(testComment.getContent(), result.getContent());
        assertEquals(testUser, result.getUser());
        assertEquals(testPost, result.getPost());
        verify(commentRepository).save(any(Comment.class));
        verify(notificationService).createNotification(any(), any(), any(), eq("COMMENT"));
    }

    @Test
    void createComment_InvalidPost_ShouldThrowException() {
        // Arrange
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.createComment(999L, 1L, "Test comment");
        });
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void getCommentById_ExistingComment_ShouldReturnComment() {
        // Arrange
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));

        // Act
        Comment result = commentService.getCommentById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testComment.getId(), result.getId());
        assertEquals(testComment.getContent(), result.getContent());
    }

    @Test
    void getCommentById_NonExistingComment_ShouldThrowException() {
        // Arrange
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.getCommentById(999L);
        });
    }

    @Test
    void updateComment_ValidUpdate_ShouldUpdateSuccessfully() {
        // Arrange
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        // Act
        Comment result = commentService.updateComment(1L, testUser.getId(), "Updated content");

        // Assert
        assertEquals("Updated content", result.getContent());
        assertTrue(result.isEdited());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void updateComment_UnauthorizedUser_ShouldThrowException() {
        // Arrange
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            commentService.updateComment(1L, 999L, "Updated content");
        });
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void getPostComments_ShouldReturnComments() {
        // Arrange
        List<Comment> comments = Arrays.asList(testComment);
        Page<Comment> commentPage = new PageImpl<>(comments);
        
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(commentRepository.findByPostOrderByCreatedAtDesc(eq(testPost), any(Pageable.class)))
                .thenReturn(commentPage);

        // Act
        Page<Comment> result = commentService.getPostComments(1L, Pageable.unpaged());

        // Assert
        assertEquals(1, result.getContent().size());
        assertEquals(testComment.getContent(), result.getContent().get(0).getContent());
    }

    @Test
    void getUserComments_ShouldReturnComments() {
        // Arrange
        List<Comment> comments = Arrays.asList(testComment);
        Page<Comment> commentPage = new PageImpl<>(comments);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(commentRepository.findByUserOrderByCreatedAtDesc(eq(testUser), any(Pageable.class)))
                .thenReturn(commentPage);

        // Act
        Page<Comment> result = commentService.getUserComments(1L, Pageable.unpaged());

        // Assert
        assertEquals(1, result.getContent().size());
        assertEquals(testComment.getContent(), result.getContent().get(0).getContent());
    }

    @Test
    void deleteComment_AuthorizedUser_ShouldDeleteSuccessfully() {
        // Arrange
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));

        // Act
        commentService.deleteComment(1L, testUser.getId());

        // Assert
        verify(commentRepository).delete(testComment);
    }

    @Test
    void deleteComment_UnauthorizedUser_ShouldThrowException() {
        // Arrange
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            commentService.deleteComment(1L, 999L);
        });
        verify(commentRepository, never()).delete(any(Comment.class));
    }
}
