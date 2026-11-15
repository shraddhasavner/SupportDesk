package com.supportdesk.controller;

import com.supportdesk.entity.Employee;
import com.supportdesk.entity.Ticket;
import com.supportdesk.entity.TicketActivity;
import com.supportdesk.entity.TicketComment;
import com.supportdesk.entity.TicketPriority;
import com.supportdesk.entity.TicketStatus;
import com.supportdesk.security.SecurityUtils;
import com.supportdesk.service.TicketService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * TicketController: REST API endpoints for Ticket Management, Comments, and Activity History.
 * 
 * [English]:
 * - Base path: /api/tickets
 * - Protects endpoints with Role-Based Access Control (RBAC).
 * - Enforces ticket ownership: regular employees can only access tickets they created.
 * - Prevents identity spoofing: users cannot create tickets or post comments using someone else's employeeId.
 * - Restricts assignment to ADMIN, status/resolution to ADMIN and TECHNICIAN.
 * 
 * [Hinglish]:
 * - Ticket controller jisme role-based security aur identity spoofing protection lagi hui hai.
 * - Normal employee sirf apne hi ticket dekh aur comment kar sakta hai.
 * - Dusre employee ka ID bhej kar impersonate karne par server 403 Forbidden deta hai.
 */
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final SecurityUtils securityUtils;

    public TicketController(TicketService ticketService, SecurityUtils securityUtils) {
        this.ticketService = ticketService;
        this.securityUtils = securityUtils;
    }

    /**
     * Create a new IT support ticket.
     * Accessible by: Any authenticated employee.
     * Prevents identity spoofing: Non-admins cannot specify another employee's ID.
     * POST /api/tickets
     */
    @PostMapping
    public ResponseEntity<Ticket> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        Employee currentUser = securityUtils.getCurrentEmployee();

        // Enforce identity protection: Non-admins cannot create tickets on behalf of others
        if (request.getEmployeeId() == null) {
            request.setEmployeeId(currentUser.getId());
        } else if (!request.getEmployeeId().equals(currentUser.getId()) && !securityUtils.isAdmin()) {
            throw new AccessDeniedException("Identity spoofing rejected: You cannot create tickets on behalf of another employee.");
        }

        Ticket created = ticketService.createTicket(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * List all tickets (filtered).
     * Accessible by:
     * - ADMIN & TECHNICIAN: view all tickets.
     * - EMPLOYEE: views only their own tickets.
     * GET /api/tickets
     */
    @GetMapping
    public ResponseEntity<List<Ticket>> getAllTickets(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority) {
        Employee currentUser = securityUtils.getCurrentEmployee();

        // If employee, restrict to their own tickets
        if (!securityUtils.isAdmin() && !securityUtils.isTechnician()) {
            return ResponseEntity.ok(ticketService.getTicketsByEmployee(currentUser.getId(), status, priority));
        }

        List<Ticket> tickets = ticketService.getAllTickets(status, priority);
        return ResponseEntity.ok(tickets);
    }

    /**
     * List all overdue tickets whose SLA deadline has passed and are not RESOLVED or CLOSED.
     * Accessible by: ADMIN, TECHNICIAN
     * GET /api/tickets/overdue
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<Ticket>> getOverdueTickets() {
        if (!securityUtils.isAdmin() && !securityUtils.isTechnician()) {
            throw new AccessDeniedException("Only administrators and technicians can view overdue ticket reports.");
        }
        List<Ticket> overdueTickets = ticketService.getOverdueTickets();
        return ResponseEntity.ok(overdueTickets);
    }

    /**
     * Get a single ticket by its database ID.
     * Accessible by: ADMIN, TECHNICIAN, or ticket creator.
     * GET /api/tickets/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable Long id) {
        Ticket ticket = ticketService.getTicketById(id);
        Employee currentUser = securityUtils.getCurrentEmployee();

        // Enforce ownership: Employees cannot view other employees' tickets
        if (!securityUtils.isAdmin() && !securityUtils.isTechnician() &&
                !ticket.getEmployee().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied: You do not have permission to view tickets created by other employees.");
        }

        return ResponseEntity.ok(ticket);
    }

    /**
     * View SLA deadline and overdue metrics for a specific ticket.
     * Accessible by: ADMIN, TECHNICIAN, or ticket creator.
     * GET /api/tickets/{id}/sla
     */
    @GetMapping("/{id}/sla")
    public ResponseEntity<SlaStatusResponse> getTicketSlaStatus(@PathVariable Long id) {
        Ticket ticket = ticketService.getTicketById(id);
        Employee currentUser = securityUtils.getCurrentEmployee();

        if (!securityUtils.isAdmin() && !securityUtils.isTechnician() &&
                !ticket.getEmployee().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied: You do not have permission to view SLA metrics for this ticket.");
        }

        SlaStatusResponse slaStatus = ticketService.getTicketSlaStatus(id);
        return ResponseEntity.ok(slaStatus);
    }

    /**
     * Get a ticket by its business ticket number (e.g., TCK-20261014-9F2B8D).
     * Accessible by: ADMIN, TECHNICIAN, or ticket creator.
     * GET /api/tickets/number/{ticketNumber}
     */
    @GetMapping("/number/{ticketNumber}")
    public ResponseEntity<Ticket> getTicketByTicketNumber(@PathVariable String ticketNumber) {
        Ticket ticket = ticketService.getTicketByTicketNumber(ticketNumber);
        Employee currentUser = securityUtils.getCurrentEmployee();

        if (!securityUtils.isAdmin() && !securityUtils.isTechnician() &&
                !ticket.getEmployee().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied: You do not have permission to view tickets created by other employees.");
        }

        return ResponseEntity.ok(ticket);
    }

    /**
     * Assign a ticket to an IT technician.
     * Accessible by: ADMIN
     * PATCH /api/tickets/{id}/assign/{technicianId}
     */
    @PatchMapping("/{id}/assign/{technicianId}")
    public ResponseEntity<Ticket> assignTechnician(
            @PathVariable Long id,
            @PathVariable Long technicianId) {
        if (!securityUtils.isAdmin()) {
            throw new AccessDeniedException("Only administrators can assign tickets to technicians.");
        }
        Employee adminActor = securityUtils.getCurrentEmployee();
        Ticket updated = ticketService.assignTechnician(id, technicianId, adminActor);
        return ResponseEntity.ok(updated);
    }

    /**
     * Alternative query-param assignment endpoint.
     * Accessible by: ADMIN
     * PUT /api/tickets/{id}/assign?technicianId={technicianId}
     */
    @PutMapping("/{id}/assign")
    public ResponseEntity<Ticket> assignTechnicianViaParam(
            @PathVariable Long id,
            @RequestParam Long technicianId) {
        if (!securityUtils.isAdmin()) {
            throw new AccessDeniedException("Only administrators can assign tickets to technicians.");
        }
        Employee adminActor = securityUtils.getCurrentEmployee();
        Ticket updated = ticketService.assignTechnician(id, technicianId, adminActor);
        return ResponseEntity.ok(updated);
    }

    /**
     * Update ticket lifecycle status.
     * Accessible by: ADMIN, TECHNICIAN
     * PATCH /api/tickets/{id}/status?status=IN_PROGRESS
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Ticket> updateTicketStatus(
            @PathVariable Long id,
            @RequestParam TicketStatus status) {
        if (!securityUtils.isAdmin() && !securityUtils.isTechnician()) {
            throw new AccessDeniedException("Only administrators and technicians can update ticket status.");
        }
        Ticket updated = ticketService.updateTicketStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    /**
     * Add resolution notes and mark resolved.
     * Accessible by: ADMIN, TECHNICIAN
     * PATCH /api/tickets/{id}/resolve
     */
    @PatchMapping("/{id}/resolve")
    public ResponseEntity<Ticket> resolveTicket(
            @PathVariable Long id,
            @Valid @RequestBody ResolutionRequest request) {
        if (!securityUtils.isAdmin() && !securityUtils.isTechnician()) {
            throw new AccessDeniedException("Only administrators and technicians can resolve tickets.");
        }
        Ticket updated = ticketService.addResolutionNotes(id, request.getResolutionNotes(), request.isMarkResolved());
        return ResponseEntity.ok(updated);
    }

    /**
     * Add resolution notes without necessarily marking as resolved.
     * Accessible by: ADMIN, TECHNICIAN
     * PATCH /api/tickets/{id}/resolution-notes
     */
    @PatchMapping("/{id}/resolution-notes")
    public ResponseEntity<Ticket> addResolutionNotes(
            @PathVariable Long id,
            @Valid @RequestBody ResolutionRequest request) {
        if (!securityUtils.isAdmin() && !securityUtils.isTechnician()) {
            throw new AccessDeniedException("Only administrators and technicians can update resolution notes.");
        }
        Ticket updated = ticketService.addResolutionNotes(id, request.getResolutionNotes(), request.isMarkResolved());
        return ResponseEntity.ok(updated);
    }

    /**
     * Add a comment to a ticket.
     * Accessible by: Authenticated employee/technician/admin.
     * Prevents impersonation and enforces ticket ownership for employees.
     * POST /api/tickets/{ticketId}/comments
     */
    @PostMapping("/{ticketId}/comments")
    public ResponseEntity<TicketComment> addComment(
            @PathVariable Long ticketId,
            @Valid @RequestBody AddCommentRequest request) {
        Ticket ticket = ticketService.getTicketById(ticketId);
        Employee currentUser = securityUtils.getCurrentEmployee();

        // Enforce ownership: Employees cannot comment on other people's tickets
        if (!securityUtils.isAdmin() && !securityUtils.isTechnician() &&
                !ticket.getEmployee().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied: You cannot comment on a ticket that does not belong to you.");
        }

        // Enforce identity protection: Authors cannot spoof another employee ID
        if (request.getEmployeeId() == null) {
            request.setEmployeeId(currentUser.getId());
        } else if (!request.getEmployeeId().equals(currentUser.getId()) && !securityUtils.isAdmin()) {
            throw new AccessDeniedException("Identity spoofing rejected: You cannot post comments on behalf of another employee.");
        }

        TicketComment comment = ticketService.addComment(ticketId, request);
        return new ResponseEntity<>(comment, HttpStatus.CREATED);
    }

    /**
     * List all comments for a ticket, ordered chronologically (oldest first).
     * Accessible by: ADMIN, TECHNICIAN, or ticket creator.
     * GET /api/tickets/{ticketId}/comments
     */
    @GetMapping("/{ticketId}/comments")
    public ResponseEntity<List<TicketComment>> getTicketComments(@PathVariable Long ticketId) {
        Ticket ticket = ticketService.getTicketById(ticketId);
        Employee currentUser = securityUtils.getCurrentEmployee();

        // Enforce ownership: Employees cannot view comments on tickets created by others
        if (!securityUtils.isAdmin() && !securityUtils.isTechnician() &&
                !ticket.getEmployee().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied: You do not have permission to view comments for this ticket.");
        }

        List<TicketComment> comments = ticketService.getCommentsByTicket(ticketId);
        return ResponseEntity.ok(comments);
    }

    /**
     * View complete activity history/audit timeline for a ticket, ordered chronologically (oldest first).
     * Accessible by: ADMIN, TECHNICIAN, or ticket creator.
     * GET /api/tickets/{ticketId}/activities
     */
    @GetMapping("/{ticketId}/activities")
    public ResponseEntity<List<TicketActivity>> getTicketActivities(@PathVariable Long ticketId) {
        Ticket ticket = ticketService.getTicketById(ticketId);
        Employee currentUser = securityUtils.getCurrentEmployee();

        // Enforce ownership: Employees cannot view activity history on tickets created by others
        if (!securityUtils.isAdmin() && !securityUtils.isTechnician() &&
                !ticket.getEmployee().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied: You do not have permission to view activity history for this ticket.");
        }

        List<TicketActivity> activities = ticketService.getTicketActivities(ticketId);
        return ResponseEntity.ok(activities);
    }

    /**
     * Convenience alias for ticket activity history.
     * GET /api/tickets/{ticketId}/history
     */
    @GetMapping("/{ticketId}/history")
    public ResponseEntity<List<TicketActivity>> getTicketHistory(@PathVariable Long ticketId) {
        return getTicketActivities(ticketId);
    }
}
