package com.skillsharing.controller;

import com.skillsharing.model.Notification;
import com.skillsharing.model.User;
import com.skillsharing.security.CurrentUser;
import com.skillsharing.security.UserPrincipal;
import com.skillsharing.service.NotificationService;
import com.skillsharing.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<Notification>> getUserNotifications(
            @CurrentUser UserPrincipal currentUser,
            Pageable pageable) {
        User user = userService.getUserById(currentUser.getId());
        Page<Notification> notifications = notificationService.getUserNotifications(user, pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Long> getUnreadCount(@CurrentUser UserPrincipal currentUser) {
        User user = userService.getUserById(currentUser.getId());
        Long count = notificationService.getUnreadCount(user);
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{id}/mark-read")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> markNotificationAsRead(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id) {
        notificationService.markAsRead(id, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/mark-all-read")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> markAllNotificationsAsRead(@CurrentUser UserPrincipal currentUser) {
        notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok().build();
    }

    // WebSocket endpoint for real-time notifications will be added later
    // @MessageMapping("/notifications")
    // @SendTo("/topic/notifications")
    // public NotificationResponse sendNotification(NotificationRequest notificationRequest) {
    //     // Implementation for real-time notifications
    // }
}
