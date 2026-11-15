/**
 * SupportDesk - Frontend Configuration & Constants
 * Phase 7.2 JWT & Dashboard Integration
 * 
 * Centralized API endpoints and application constants.
 * Note: JWT tokens are kept strictly in JavaScript memory per Phase 7.2 requirements.
 */

const AppConfig = {
    // Base API path
    BASE_API_URL: '/api',

    // Specific Module Endpoints
    ENDPOINTS: {
        // Auth module
        LOGIN: '/api/auth/login',

        // Employee module
        EMPLOYEES: '/api/employees',
        TECHNICIANS: '/api/employees/technicians',

        // Ticket module
        TICKETS: '/api/tickets',
        TICKETS_OVERDUE: '/api/tickets/overdue',

        // Dashboard analytics module
        DASHBOARD_SUMMARY: '/api/dashboard/summary',
        DASHBOARD_PRIORITY: '/api/dashboard/priority-summary',
        DASHBOARD_WORKLOAD: '/api/dashboard/technician-workload',
        DASHBOARD_REPORTS: '/api/dashboard/reports',

        // Notification module
        NOTIFICATIONS: '/api/notifications',
        NOTIFICATIONS_UNREAD_COUNT: '/api/notifications/unread-count'
    },

    // Ticket Status enum definitions
    TICKET_STATUS: {
        OPEN: 'OPEN',
        ASSIGNED: 'ASSIGNED',
        IN_PROGRESS: 'IN_PROGRESS',
        RESOLVED: 'RESOLVED',
        CLOSED: 'CLOSED'
    },

    // Ticket Priority enum definitions
    TICKET_PRIORITY: {
        LOW: 'LOW',
        MEDIUM: 'MEDIUM',
        HIGH: 'HIGH',
        CRITICAL: 'CRITICAL'
    },

    // User Roles
    ROLES: {
        ADMIN: 'ADMIN',
        TECHNICIAN: 'TECHNICIAN',
        EMPLOYEE: 'EMPLOYEE'
    }
};

// Freeze object to prevent accidental runtime mutations
Object.freeze(AppConfig);
