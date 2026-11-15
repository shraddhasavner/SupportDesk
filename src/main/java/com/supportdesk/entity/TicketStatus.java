package com.supportdesk.entity;

/**
 * TicketStatus Enum: Tracks the lifecycle stage of an IT support ticket.
 * 
 * [English]:
 * - OPEN: Newly raised ticket awaiting review and technician assignment.
 * - ASSIGNED: An IT technician has been designated to investigate the issue.
 * - IN_PROGRESS: Technician is actively working on diagnosis or repair.
 * - RESOLVED: Technician has completed the fix and added resolution notes.
 * - CLOSED: Employee or admin has verified the solution and closed the ticket.
 * 
 * [Hinglish]:
 * - OPEN: Naya ticket jo abhi kisi ko assign nahi hua hai.
 * - ASSIGNED: Technician ko assign kiya ja chuka hai.
 * - IN_PROGRESS: Technician issue par active kaam kar raha hai.
 * - RESOLVED: Issue fix ho chuka hai aur solution likh diya gaya hai.
 * - CLOSED: Ticket final close ho chuka hai.
 */
public enum TicketStatus {
    OPEN,
    ASSIGNED,
    IN_PROGRESS,
    RESOLVED,
    CLOSED
}
