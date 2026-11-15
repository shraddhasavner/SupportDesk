package com.supportdesk.entity;

/**
 * Role Enum: Defines the access levels in the SupportDesk system.
 * 
 * [English]:
 * - EMPLOYEE: Regular staff member who can create and view their own IT support tickets.
 * - TECHNICIAN: IT support staff assigned to troubleshoot and resolve tickets.
 * - ADMIN: System administrator who manages employees, ticket assignments, and system settings.
 * 
 * [Hinglish]:
 * - EMPLOYEE: Aam employee jo apni IT problems ke liye ticket raise karta hai.
 * - TECHNICIAN: IT support person jo ticket ko solve karta hai aur update deta hai.
 * - ADMIN: Main manager jo employees ko manage karta hai aur technicians ko ticket assign karta hai.
 */
public enum Role {
    EMPLOYEE,
    TECHNICIAN,
    ADMIN
}
