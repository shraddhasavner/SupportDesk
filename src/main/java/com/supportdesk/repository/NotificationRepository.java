package com.supportdesk.repository;

import com.supportdesk.entity.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * NotificationRepository: Spring Data JPA repository for Notification entities.
 * 
 * [English]:
 * - Retrieves user notifications ordered newest first using explicit JPQL queries.
 * - Computes unread count for badge indicators.
 * - Performs atomic batch update to mark all unread notifications as read.
 * 
 * [Hinglish]:
 * - Notifications table ke queries chalane ke liye repository.
 * - Newest notifications pehle dikhane aur unread count nikalne ke methods deta hai.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Retrieve all notifications for a specific recipient, ordered newest first.
     */
    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :recipientId ORDER BY n.createdAt DESC, n.id DESC")
    List<Notification> findByRecipientIdOrderByCreatedAtDescIdDesc(@Param("recipientId") Long recipientId);

    /**
     * Count unread notifications for a specific recipient.
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient.id = :recipientId AND n.read = false")
    long countByRecipientIdAndReadFalse(@Param("recipientId") Long recipientId);

    /**
     * Find unread notifications for a recipient.
     */
    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :recipientId AND n.read = false")
    List<Notification> findByRecipientIdAndReadFalse(@Param("recipientId") Long recipientId);

    /**
     * Mark all unread notifications for a recipient as read.
     */
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.recipient.id = :recipientId AND n.read = false")
    int markAllAsReadByRecipientId(@Param("recipientId") Long recipientId);
}
