package com.supportdesk.entity;

/**
 * ActivityType: Enum representing the type of event recorded in a ticket's audit trail.
 * 
 * [English]:
 * Defines the distinct lifecycle actions that get recorded into the activity log:
 * - TICKET_CREATED: When an employee first raises a ticket.
 * - TECHNICIAN_ASSIGNED: When a technician is assigned to work on the ticket.
 * - STATUS_CHANGED: When the ticket transitions between lifecycle statuses (e.g. ASSIGNED -> IN_PROGRESS).
 * - TICKET_RESOLVED: When resolution notes are provided and the ticket is marked resolved.
 * - COMMENT_ADDED: When an employee or technician posts a comment on the ticket.
 * 
 * [Hinglish]:
 * Ticket ke sath hone wale alag-alag events ko track karne ke liye enum.
 * Isse audit trail me pata chalta hai ki ticket par kis tarah ka action perform kiya gaya hai.
 */
public enum ActivityType {
    TICKET_CREATED,
    TECHNICIAN_ASSIGNED,
    STATUS_CHANGED,
    TICKET_RESOLVED,
    COMMENT_ADDED
}
