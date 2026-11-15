package com.supportdesk.controller;

import com.supportdesk.entity.Employee;
import com.supportdesk.entity.Notification;
import com.supportdesk.security.SecurityUtils;
import com.supportdesk.service.NotificationService;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * NotificationController: REST API endpoints for in-app user notifications.
 * 
 * [English]:
 * - Base path: /api/notifications
 * - All endpoints derive user identity securely from the server-side SecurityContext (SecurityUtils).
 * - Client-supplied employee IDs are never accepted or trusted.
 * - Endpoints:
 *   * GET /api/notifications: Retrieve current authenticated user's notifications (newest first).
 *   * GET /api/notifications/unread-count: Retrieve unread notification count for current user.
 *   * PATCH /api/notifications/{id}/read: Mark a notification as read (with strict ownership check).
 *   * PATCH /api/notifications/read-all: Mark all unread notifications for current user as read.
 * 
 * [Hinglish]:
 * - Notifications controller jisme user identity securely JWT token se nikaali jaati hai.
 * - Koi bhi user kisi dusre ka notification na dekh sakta hai na hi mark as read kar sakta hai.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final SecurityUtils securityUtils;

    public NotificationController(NotificationService notificationService, SecurityUtils securityUtils) {
        this.notificationService = notificationService;
        this.securityUtils = securityUtils;
    }

    /**
     * Retrieve all notifications for the authenticated user, ordered newest first.
     * GET /api/notifications
     */
    @GetMapping
    public ResponseEntity<List<Notification>> getCurrentUserNotifications() {
        Employee currentUser = securityUtils.getCurrentEmployee();
        List<Notification> notifications = notificationService.getUserNotifications(currentUser.getId());
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get count of unread notifications for the authenticated user.
     * GET /api/notifications/unread-count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount() {
        Employee currentUser = securityUtils.getCurrentEmployee();
        long unreadCount = notificationService.getUnreadCount(currentUser.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("unreadCount", unreadCount);
        return ResponseEntity.ok(response);
    }

    /**
     * Mark a specific notification as read.
     * Enforces recipient ownership (throws 403 Forbidden if not the recipient).
     * PATCH /api/notifications/{id}/read
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long id) {
        Employee currentUser = securityUtils.getCurrentEmployee();
        Notification updated = notificationService.markAsRead(id, currentUser.getId());
        return ResponseEntity.ok(updated);
    }

    /**
     * Mark all unread notifications for the authenticated user as read.
     * PATCH /api/notifications/read-all
     */
    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead() {
        Employee currentUser = securityUtils.getCurrentEmployee();
        int count = notificationService.markAllAsRead(currentUser.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "All notifications marked as read.");
        response.put("updatedCount", count);
        return ResponseEntity.ok(response);
    }
}
