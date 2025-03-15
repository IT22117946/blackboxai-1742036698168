package com.skillsharing.controller;

import com.skillsharing.BaseTest;
import com.skillsharing.model.Notification;
import com.skillsharing.model.Post;
import com.skillsharing.service.NotificationService;
import com.skillsharing.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class NotificationControllerTest extends BaseTest {

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private UserService userService;

    @Test
    void getUserNotifications_ShouldReturnNotifications() throws Exception {
        // Arrange
        List<Notification> notifications = Arrays.asList(
            createTestNotification(Notification.NotificationType.LIKE),
            createTestNotification(Notification.NotificationType.COMMENT)
        );
        Page<Notification> notificationPage = new PageImpl<>(notifications);
        
        when(userService.getUserById(testUser.getId())).thenReturn(testUser);
        when(notificationService.getUserNotifications(eq(testUser), any(Pageable.class)))
                .thenReturn(notificationPage);

        // Act & Assert
        mockMvc.perform(get("/api/notifications")
                .header("Authorization", getAuthHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].type").value("LIKE"))
                .andExpect(jsonPath("$.content[1].type").value("COMMENT"));
    }

    @Test
    void getUnreadCount_ShouldReturnCount() throws Exception {
        // Arrange
        when(userService.getUserById(testUser.getId())).thenReturn(testUser);
        when(notificationService.getUnreadCount(testUser)).thenReturn(5L);

        // Act & Assert
        mockMvc.perform(get("/api/notifications/unread-count")
                .header("Authorization", getAuthHeader()))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    @Test
    void markNotificationAsRead_ShouldMarkAsRead() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/notifications/{id}/mark-read", 1L)
                .header("Authorization", getAuthHeader()))
                .andExpect(status().isOk());
    }

    @Test
    void markAllNotificationsAsRead_ShouldMarkAllAsRead() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/notifications/mark-all-read")
                .header("Authorization", getAuthHeader()))
                .andExpect(status().isOk());
    }

    private Notification createTestNotification(Notification.NotificationType type) {
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setUser(testUser);
        notification.setActor(createAnotherTestUser());
        notification.setPost(createTestPost());
        notification.setType(type);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }

    private User createAnotherTestUser() {
        User user = new User();
        user.setId(2L);
        user.setName("Another Test User");
        user.setEmail("another.test@example.com");
        user.setProvider(User.AuthProvider.LOCAL);
        user.setProviderId("local");
        return user;
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
