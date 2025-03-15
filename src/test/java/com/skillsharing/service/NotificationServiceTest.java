package com.skillsharing.service;

import com.skillsharing.exception.ResourceNotFoundException;
import com.skillsharing.model.Notification;
import com.skillsharing.model.Post;
import com.skillsharing.model.User;
import com.skillsharing.repository.NotificationRepository;
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
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User testUser;
    private User testActor;
    private Post testPost;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");

        testActor = new User();
        testActor.setId(2L);
        testActor.setName("Test Actor");
        testActor.setEmail("actor@example.com");

        testPost = new Post();
        testPost.setId(1L);
        testPost.setUser(testUser);
        testPost.setDescription("Test post description");

        testNotification = new Notification();
        testNotification.setId(1L);
        testNotification.setUser(testUser);
        testNotification.setActor(testActor);
        testNotification.setPost(testPost);
        testNotification.setType(Notification.NotificationType.LIKE);
        testNotification.setRead(false);
        testNotification.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createNotification_ValidData_ShouldCreateSuccessfully() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // Act
        Notification result = notificationService.createNotification(
            testUser, testActor, testPost, "LIKE"
        );

        // Assert
        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertEquals(testActor, result.getActor());
        assertEquals(testPost, result.getPost());
        assertEquals(Notification.NotificationType.LIKE, result.getType());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void getNotificationById_ExistingNotification_ShouldReturnNotification() {
        // Arrange
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));

        // Act
        Notification result = notificationService.getNotificationById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testNotification.getId(), result.getId());
        assertEquals(testNotification.getType(), result.getType());
    }

    @Test
    void getNotificationById_NonExistingNotification_ShouldThrowException() {
        // Arrange
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            notificationService.getNotificationById(999L);
        });
    }

    @Test
    void getUserNotifications_ShouldReturnNotifications() {
        // Arrange
        List<Notification> notifications = Arrays.asList(testNotification);
        Page<Notification> notificationPage = new PageImpl<>(notifications);
        
        when(notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(
            eq(testUser), any(Pageable.class))).thenReturn(notificationPage);

        // Act
        Page<Notification> result = notificationService.getUserNotifications(testUser, Pageable.unpaged());

        // Assert
        assertEquals(1, result.getContent().size());
        assertEquals(testNotification.getType(), result.getContent().get(0).getType());
    }

    @Test
    void getUnreadCount_ShouldReturnCount() {
        // Arrange
        when(notificationRepository.countByUserAndReadFalse(testUser)).thenReturn(5L);

        // Act
        Long result = notificationService.getUnreadCount(testUser);

        // Assert
        assertEquals(5L, result);
    }

    @Test
    void markAsRead_ValidNotification_ShouldMarkAsRead() {
        // Arrange
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // Act
        notificationService.markAsRead(1L, testUser.getId());

        // Assert
        assertTrue(testNotification.isRead());
        verify(notificationRepository).save(testNotification);
    }

    @Test
    void markAsRead_UnauthorizedUser_ShouldThrowException() {
        // Arrange
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            notificationService.markAsRead(1L, 999L);
        });
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void markAllAsRead_ShouldMarkAllAsRead() {
        // Act
        notificationService.markAllAsRead(testUser.getId());

        // Assert
        verify(notificationRepository).markAllAsRead(testUser.getId());
    }

    @Test
    void createFollowNotification_ShouldCreateNotification() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // Act
        notificationService.createFollowNotification(testActor, testUser);

        // Assert
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void cleanupOldNotifications_ShouldDeleteOldNotifications() {
        // Act
        notificationService.cleanupOldNotifications();

        // Assert
        verify(notificationRepository).deleteOldNotifications(any(LocalDateTime.class));
    }
}
