package com.supportdesk.entity;

/**
 * NotificationType: Enum defining the event categories that generate user notifications.
 * 
 * [English]:
 * - TECHNICIAN_ASSIGNED: Fired when an IT technician is assigned to a ticket.
 * - STATUS_CHANGED: Fired when a ticket's status changes (e.g. OPEN -> IN_PROGRESS).
 * - COMMENT_ADDED: Fired when a comment is posted to a ticket.
 * - TICKET_RESOLVED: Fired when resolution notes are added and the ticket is marked resolved.
 * 
 * [Hinglish]:
 * - Notification category specify karne ke liye enum.
 * - Technician assign hone par, status badalne par, naya comment aane par ya ticket resolve hone par generate hota hai.
 */
public enum NotificationType {
    TECHNICIAN_ASSIGNED,
    STATUS_CHANGED,
    COMMENT_ADDED,
    TICKET_RESOLVED
}
