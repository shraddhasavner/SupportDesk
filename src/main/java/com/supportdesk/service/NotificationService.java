package com.supportdesk.service;

import com.supportdesk.entity.Employee;
import com.supportdesk.entity.Notification;
import com.supportdesk.entity.NotificationType;
import com.supportdesk.entity.Ticket;
import com.supportdesk.repository.NotificationRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * NotificationService: Handles business logic for generating, retrieving, and marking user notifications.
 * 
 * [English]:
 * - Generates notifications for relevant participants upon ticket assignment, status updates, comments, and resolution.
 * - Prevents duplicate notifications when an operation does not change ticket state.
 * - Suppresses notifications to the actor performing the action (no self-notifications).
 * - Enforces recipient ownership when marking notifications as read.
 * - Provides batch mark-all-as-read functionality.
 * 
 * [Hinglish]:
 * - Notifications generate karne, unread count nikalne aur mark as read karne ki service.
 * - Jo user khud action perform karta hai usko notification nahi bhejti (self-notification avoidance).
 * - Dusre user ke notification ko koi aur mark-as-read nahi kar sakta (ownership verification).
 */
@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Send an individual notification to a specific recipient.
     */
    public Notification sendNotification(Employee recipient, Ticket ticket, String title, String message, NotificationType type) {
        if (recipient == null) {
            return null;
        }
        Notification notification = new Notification(recipient, ticket, title, message, type);
        return notificationRepository.save(notification);
    }

    /**
     * Notify relevant participants (ticket creator and assigned technician) about an event.
     * Guarantees:
     * 1. The actor performing the action does not receive a notification.
     * 2. If creator and technician are the same user, only one notification is sent.
     * 3. Null participants are ignored.
     */
    public void notifyTicketParticipants(Ticket ticket, Employee actor, String title, String message, NotificationType type) {
        if (ticket == null) {
            return;
        }

        Set<Long> notifiedIds = new HashSet<>();
        Long actorId = actor != null ? actor.getId() : null;

        // 1. Notify ticket creator
        Employee creator = ticket.getEmployee();
        if (creator != null && (actorId == null || !creator.getId().equals(actorId))) {
            sendNotification(creator, ticket, title, message, type);
            notifiedIds.add(creator.getId());
        }

        // 2. Notify assigned technician (if distinct from creator and actor)
        Employee technician = ticket.getAssignedTechnician();
        if (technician != null && !notifiedIds.contains(technician.getId()) && (actorId == null || !technician.getId().equals(actorId))) {
            sendNotification(technician, ticket, title, message, type);
            notifiedIds.add(technician.getId());
        }
    }

    /**
     * Specialized notification trigger for technician assignment.
     */
    public void notifyTechnicianAssignment(Ticket ticket, Employee technician, Employee actor) {
        if (ticket == null || technician == null) {
            return;
        }

        Long actorId = actor != null ? actor.getId() : null;
        Set<Long> notifiedIds = new HashSet<>();

        // 1. Notify the assigned technician
        if (actorId == null || !technician.getId().equals(actorId)) {
            String techTitle = "Technician Assigned";
            String techMessage = "You have been assigned to ticket #" + ticket.getTicketNumber() + ": " + ticket.getTitle();
            sendNotification(technician, ticket, techTitle, techMessage, NotificationType.TECHNICIAN_ASSIGNED);
            notifiedIds.add(technician.getId());
        }

        // 2. Notify ticket creator if distinct
        Employee creator = ticket.getEmployee();
        if (creator != null && !notifiedIds.contains(creator.getId()) && (actorId == null || !creator.getId().equals(actorId))) {
            String creatorTitle = "Technician Assigned";
            String creatorMessage = "Technician " + technician.getName() + " has been assigned to your ticket #" + ticket.getTicketNumber() + ".";
            sendNotification(creator, ticket, creatorTitle, creatorMessage, NotificationType.TECHNICIAN_ASSIGNED);
            notifiedIds.add(creator.getId());
        }
    }

    /**
     * Retrieve all notifications for a given user, ordered newest first.
     */
    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(Long recipientId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDescIdDesc(recipientId);
    }

    /**
     * Count unread notifications for a given user.
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long recipientId) {
        return notificationRepository.countByRecipientIdAndReadFalse(recipientId);
    }

    /**
     * Mark a single notification as read, enforcing recipient ownership.
     * Throws 404 if notification not found, 403 if authenticated user is not the recipient.
     */
    public Notification markAsRead(Long notificationId, Long currentUserId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));

        if (!notification.getRecipient().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Access denied: You do not have permission to modify this notification.");
        }

        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    /**
     * Mark all unread notifications for a given user as read.
     */
    public int markAllAsRead(Long currentUserId) {
        return notificationRepository.markAllAsReadByRecipientId(currentUserId);
    }
}
