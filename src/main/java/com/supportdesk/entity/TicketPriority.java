package com.supportdesk.entity;

/**
 * TicketPriority Enum: Defines the urgency level of an IT support ticket.
 * 
 * [English]:
 * - LOW: Minor issue or general query that does not hinder daily work.
 * - MEDIUM: Standard issue affecting a single user with a temporary workaround.
 * - HIGH: Significant issue blocking critical work for an employee.
 * - CRITICAL: System-wide outage or severe security/hardware issue requiring immediate resolution.
 * 
 * [Hinglish]:
 * - LOW: Chhoti problem jisse kaam rukta nahi hai.
 * - MEDIUM: Normal issue jo regular priority par solve hota hai.
 * - HIGH: Bada issue jisse user ka important kaam ruk gaya ho.
 * - CRITICAL: Emergency issue (jaise server down ya major system crash) jise turant fix karna zaroori hai.
 */
public enum TicketPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
