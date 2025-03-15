package com.skillsharing.repository;

import com.skillsharing.model.Notification;
import com.skillsharing.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Find notifications for a user
    Page<Comment> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // Find unread notifications for a user
    Page<Notification> findByUserAndReadFalseOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // Count unread notifications for a user
    Long countByUserAndReadFalse(User user);
    
    // Mark all notifications as read for a user
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.user.id = :userId AND n.read = false")
    void markAllAsRead(@Param("userId") Long userId);
    
    // Delete old notifications
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.read = true AND n.createdAt < :date")
    void deleteOldNotifications(@Param("date") java.time.LocalDateTime date);
}
