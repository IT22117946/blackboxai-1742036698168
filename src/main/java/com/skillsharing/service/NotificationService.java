package com.skillsharing.service;

import com.skillsharing.model.Notification;
import com.skillsharing.model.Post;
import com.skillsharing.model.User;
import com.skillsharing.repository.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Notification createNotification(User recipient, User actor, Post post, String type) {
        Notification notification = new Notification();
        notification.setUser(recipient);
        notification.setActor(actor);
        notification.setPost(post);
        notification.setType(Notification.NotificationType.valueOf(type));
        
        return notificationRepository.save(notification);
    }

    public Notification getNotificationById(Long id) {
        return notificationRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Notification not found with id: " + id));
    }

    public Page<Notification> getUserNotifications(User user, Pageable pageable) {
        return notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user, pageable);
    }

    public Long getUnreadCount(User user) {
        return notificationRepository.countByUserAndReadFalse(user);
    }

    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = getNotificationById(notificationId);
        
        // Verify ownership
        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("User does not own this notification");
        }
        
        notification.markAsRead();
        notificationRepository.save(notification);
    }

    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    // Scheduled task to clean up old notifications (runs daily at midnight)
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupOldNotifications() {
        // Delete read notifications older than 30 days
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        notificationRepository.deleteOldNotifications(thirtyDaysAgo);
    }

    // Create follow notification
    public void createFollowNotification(User follower, User following) {
        Notification notification = new Notification();
        notification.setUser(following);
        notification.setActor(follower);
        notification.setType(Notification.NotificationType.FOLLOW);
        
        notificationRepository.save(notification);
    }

    // Create like notification
    public void createLikeNotification(User liker, Post post) {
        // Don't create notification if user likes their own post
        if (!post.getUser().getId().equals(liker.getId())) {
            createNotification(post.getUser(), liker, post, "LIKE");
        }
    }

    // Create comment notification
    public void createCommentNotification(User commenter, Post post) {
        // Don't create notification if user comments on their own post
        if (!post.getUser().getId().equals(commenter.getId())) {
            createNotification(post.getUser(), commenter, post, "COMMENT");
        }
    }
}
