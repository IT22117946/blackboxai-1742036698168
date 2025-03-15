package com.skillsharing.service;

import com.skillsharing.exception.ResourceNotFoundException;
import com.skillsharing.model.Post;
import com.skillsharing.model.User;
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
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PostService postService;

    private User testUser;
    private Post testPost;

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
        testPost.setPostType(Post.PostType.SKILL_SHARING);
        testPost.setCreatedAt(LocalDateTime.now());
        testPost.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void createPost_ValidPost_ShouldCreateSuccessfully() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        // Act
        Post result = postService.createPost(testPost, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(testPost.getDescription(), result.getDescription());
        assertEquals(testUser, result.getUser());
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void createPost_InvalidUser_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            postService.createPost(testPost, 999L);
        });
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void getPostById_ExistingPost_ShouldReturnPost() {
        // Arrange
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

        // Act
        Post result = postService.getPostById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testPost.getId(), result.getId());
        assertEquals(testPost.getDescription(), result.getDescription());
    }

    @Test
    void getPostById_NonExistingPost_ShouldThrowException() {
        // Arrange
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            postService.getPostById(999L);
        });
    }

    @Test
    void updatePost_ValidUpdate_ShouldUpdateSuccessfully() {
        // Arrange
        Post updatedPost = new Post();
        updatedPost.setDescription("Updated description");
        
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        // Act
        Post result = postService.updatePost(1L, updatedPost, testUser.getId());

        // Assert
        assertEquals(updatedPost.getDescription(), result.getDescription());
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void likePost_ShouldLikeSuccessfully() {
        // Arrange
        User liker = new User();
        liker.setId(2L);
        
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findById(2L)).thenReturn(Optional.of(liker));
        when(postRepository.hasUserLikedPost(1L, 2L)).thenReturn(false);

        // Act
        postService.likePost(1L, 2L);

        // Assert
        verify(postRepository).save(any(Post.class));
        verify(notificationService).createNotification(any(), any(), any(), eq("LIKE"));
    }

    @Test
    void getUserPosts_ShouldReturnUserPosts() {
        // Arrange
        List<Post> posts = Arrays.asList(testPost);
        Page<Post> postPage = new PageImpl<>(posts);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(postRepository.findByUserOrderByCreatedAtDesc(eq(testUser), any(Pageable.class)))
                .thenReturn(postPage);

        // Act
        Page<Post> result = postService.getUserPosts(1L, Pageable.unpaged());

        // Assert
        assertEquals(1, result.getContent().size());
        assertEquals(testPost.getDescription(), result.getContent().get(0).getDescription());
    }

    @Test
    void searchPosts_ShouldReturnMatchingPosts() {
        // Arrange
        List<Post> posts = Arrays.asList(testPost);
        Page<Post> postPage = new PageImpl<>(posts);
        
        when(postRepository.searchPosts(eq("test"), any(Pageable.class)))
                .thenReturn(postPage);

        // Act
        Page<Post> result = postService.searchPosts("test", Pageable.unpaged());

        // Assert
        assertEquals(1, result.getContent().size());
        assertEquals(testPost.getDescription(), result.getContent().get(0).getDescription());
    }

    @Test
    void getActiveLearningPlans_ShouldReturnActivePlans() {
        // Arrange
        List<Post> plans = Arrays.asList(testPost);
        when(postRepository.findActiveLearningPlans()).thenReturn(plans);

        // Act
        List<Post> result = postService.getActiveLearningPlans();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testPost.getDescription(), result.get(0).getDescription());
    }
}
