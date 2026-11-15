package com.supportdesk.service;

import com.supportdesk.controller.AddCommentRequest;
import com.supportdesk.controller.CreateTicketRequest;
import com.supportdesk.controller.SlaStatusResponse;
import com.supportdesk.entity.ActivityType;
import com.supportdesk.entity.Employee;
import com.supportdesk.entity.Role;
import com.supportdesk.entity.Ticket;
import com.supportdesk.entity.TicketActivity;
import com.supportdesk.entity.TicketComment;
import com.supportdesk.entity.TicketPriority;
import com.supportdesk.entity.TicketStatus;
import com.supportdesk.entity.NotificationType;
import com.supportdesk.repository.EmployeeRepository;
import com.supportdesk.repository.TicketActivityRepository;
import com.supportdesk.repository.TicketCommentRepository;
import com.supportdesk.repository.TicketRepository;
import com.supportdesk.security.SecurityUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * TicketService: Handles business logic for support tickets, comments, and activity history.
 * 
 * [English]:
 * - Generates collision-resistant unique ticket numbers (date + random hex).
 * - Enforces assignment rules: only active employees with the TECHNICIAN role can be assigned.
 * - Handles ticket lifecycle transitions (OPEN -> ASSIGNED -> IN_PROGRESS -> RESOLVED -> CLOSED).
 * - Automatically records audit events (TicketActivity) for ticket creation, technician assignment,
 *   status transitions, resolution notes, and new comments without logging duplicate events.
 * - Manages ticket comments and guarantees chronological ordering.
 * - Dispatches database-backed in-app notifications to relevant participants.
 * 
 * [Hinglish]:
 * - Ticket business logic, comments aur activity audit history ko manage karta hai.
 * - Ticket bante hi, assign hote hi, status badalne par aur comment aane par automatically
 *   activity timeline (TicketActivity) me record karta hai aur notifications bhejta hai.
 * - Sirf active TECHNICIAN ko hi ticket assign hone deta hai.
 * - Comments ko chronologically oldest-first order me retrieve karta hai.
 */
@Service
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final EmployeeRepository employeeRepository;
    private final TicketCommentRepository ticketCommentRepository;
    private final TicketActivityRepository ticketActivityRepository;
    private final NotificationService notificationService;
    private final SecurityUtils securityUtils;

    @Value("${app.sla.critical-hours:4}")
    private long criticalSlaHours = 4;

    @Value("${app.sla.high-hours:8}")
    private long highSlaHours = 8;

    @Value("${app.sla.medium-hours:24}")
    private long mediumSlaHours = 24;

    @Value("${app.sla.low-hours:48}")
    private long lowSlaHours = 48;

    @Autowired
    public TicketService(TicketRepository ticketRepository,
                         EmployeeRepository employeeRepository,
                         TicketCommentRepository ticketCommentRepository,
                         TicketActivityRepository ticketActivityRepository,
                         @Autowired(required = false) NotificationService notificationService,
                         @Autowired(required = false) SecurityUtils securityUtils) {
        this.ticketRepository = ticketRepository;
        this.employeeRepository = employeeRepository;
        this.ticketCommentRepository = ticketCommentRepository;
        this.ticketActivityRepository = ticketActivityRepository;
        this.notificationService = notificationService;
        this.securityUtils = securityUtils;
    }

    public TicketService(TicketRepository ticketRepository,
                         EmployeeRepository employeeRepository,
                         TicketCommentRepository ticketCommentRepository,
                         TicketActivityRepository ticketActivityRepository) {
        this(ticketRepository, employeeRepository, ticketCommentRepository, ticketActivityRepository, null, null);
    }

    /**
     * Create a new ticket with a safe unique ticket number, automatic SLA deadline calculation,
     * and an initial TICKET_CREATED activity record.
     */
    public Ticket createTicket(CreateTicketRequest request) {
        Employee creator = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + request.getEmployeeId()));

        if (!creator.isActive()) {
            throw new IllegalArgumentException("Cannot create a ticket for an inactive employee (ID: " + creator.getId() + ").");
        }

        String uniqueTicketNumber = generateSafeTicketNumber();

        Ticket ticket = new Ticket();
        ticket.setTicketNumber(uniqueTicketNumber);
        ticket.setTitle(request.getTitle().trim());
        ticket.setDescription(request.getDescription().trim());
        ticket.setCategory(request.getCategory().trim());
        ticket.setPriority(request.getPriority() != null ? request.getPriority() : TicketPriority.MEDIUM);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setEmployee(creator);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());

        // Automatically calculate and set SLA deadline based on priority
        ticket.setSlaDeadline(calculateSlaDeadline(ticket.getPriority(), ticket.getCreatedAt()));

        Ticket savedTicket = ticketRepository.save(ticket);

        // Record initial activity log
        recordActivity(savedTicket, creator, ActivityType.TICKET_CREATED,
                "Ticket created with priority " + savedTicket.getPriority());

        return savedTicket;
    }

    /**
     * List tickets with optional status and priority filters.
     */
    @Transactional(readOnly = true)
    public List<Ticket> getAllTickets(TicketStatus status, TicketPriority priority) {
        if (status != null && priority != null) {
            return ticketRepository.findByStatusAndPriority(status, priority);
        } else if (status != null) {
            return ticketRepository.findByStatus(status);
        } else if (priority != null) {
            return ticketRepository.findByPriority(priority);
        }
        return ticketRepository.findAll();
    }

    /**
     * List tickets created by a specific employee with optional status and priority filters.
     */
    @Transactional(readOnly = true)
    public List<Ticket> getTicketsByEmployee(Long employeeId, TicketStatus status, TicketPriority priority) {
        if (status != null && priority != null) {
            return ticketRepository.findByEmployeeIdAndStatusAndPriority(employeeId, status, priority);
        } else if (status != null) {
            return ticketRepository.findByEmployeeIdAndStatus(employeeId, status);
        } else if (priority != null) {
            return ticketRepository.findByEmployeeIdAndPriority(employeeId, priority);
        }
        return ticketRepository.findByEmployeeId(employeeId);
    }

    /**
     * Get ticket by ID or throw ResourceNotFoundException.
     */
    @Transactional(readOnly = true)
    public Ticket getTicketById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + id));
    }

    /**
     * Get ticket by its unique ticket number (e.g. "TCK-20261014-A1B2C3").
     */
    @Transactional(readOnly = true)
    public Ticket getTicketByTicketNumber(String ticketNumber) {
        return ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ticket number: " + ticketNumber));
    }

    /**
     * Assign ticket to an IT technician.
     * Enforces:
     * 1. Technician employee exists.
     * 2. Technician must have Role.TECHNICIAN.
     * 3. Technician must be active.
     * Prevents recording duplicate activity if the ticket is already assigned to the same technician.
     * Resolves authenticated user from SecurityContext to record as the activity actor.
     */
    public Ticket assignTechnician(Long ticketId, Long technicianId) {
        Employee actor = securityUtils != null ? securityUtils.getCurrentEmployeeOrNull() : null;
        return assignTechnician(ticketId, technicianId, actor);
    }

    /**
     * Assign ticket to an IT technician with explicit actor attribution.
     * Records the acting employee as the activity actor and preserves the technician as the assignment target.
     */
    public Ticket assignTechnician(Long ticketId, Long technicianId, Employee actor) {
        Ticket ticket = getTicketById(ticketId);

        Employee technician = employeeRepository.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found with ID: " + technicianId));

        if (!technician.isActive()) {
            throw new IllegalArgumentException("Cannot assign ticket to inactive employee (ID: " + technicianId + ").");
        }

        if (technician.getRole() != Role.TECHNICIAN) {
            throw new IllegalArgumentException("Employee with ID " + technicianId + " does not have the TECHNICIAN role (Current role: " + technician.getRole() + ").");
        }

        // Check if already assigned to the same technician without any status change
        boolean alreadyAssignedToSameTech = ticket.getAssignedTechnician() != null &&
                ticket.getAssignedTechnician().getId().equals(technicianId);
        if (alreadyAssignedToSameTech && ticket.getStatus() != TicketStatus.OPEN) {
            // No state change occurs; do not record duplicate activity
            return ticket;
        }

        ticket.setAssignedTechnician(technician);

        // If ticket was OPEN, move status to ASSIGNED automatically
        if (ticket.getStatus() == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.ASSIGNED);
        }

        ticket.setUpdatedAt(LocalDateTime.now());
        Ticket savedTicket = ticketRepository.save(ticket);

        // Record activity history with authenticated actor (or fallback to technician if unauthenticated)
        Employee performedBy = actor != null ? actor : technician;
        recordActivity(savedTicket, performedBy, ActivityType.TECHNICIAN_ASSIGNED,
                "Ticket assigned to technician " + technician.getName());

        // Dispatch notifications to relevant participants
        if (notificationService != null) {
            notificationService.notifyTechnicianAssignment(savedTicket, technician, actor);
        }

        return savedTicket;
    }

    /**
     * Update ticket lifecycle status (OPEN, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED).
     * Avoids recording duplicate activity if the ticket is already in the requested status.
     */
    public Ticket updateTicketStatus(Long ticketId, TicketStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Ticket status cannot be null.");
        }

        Ticket ticket = getTicketById(ticketId);

        // Prevent duplicate status update and duplicate activity logging
        if (ticket.getStatus() == newStatus) {
            return ticket;
        }

        TicketStatus oldStatus = ticket.getStatus();
        ticket.setStatus(newStatus);
        ticket.setUpdatedAt(LocalDateTime.now());
        Ticket savedTicket = ticketRepository.save(ticket);

        // Record activity history with actual authenticated actor (or fallback to assigned technician/employee)
        Employee actor = securityUtils != null ? securityUtils.getCurrentEmployeeOrNull() : null;
        Employee performedBy = actor != null ? actor : (savedTicket.getAssignedTechnician() != null ? savedTicket.getAssignedTechnician() : savedTicket.getEmployee());
        recordActivity(savedTicket, performedBy, ActivityType.STATUS_CHANGED,
                "Ticket status changed from " + oldStatus + " to " + newStatus);

        // Dispatch notifications to relevant participants
        if (notificationService != null) {
            NotificationType notifType = (newStatus == TicketStatus.RESOLVED) ? NotificationType.TICKET_RESOLVED : NotificationType.STATUS_CHANGED;
            String notifTitle = (newStatus == TicketStatus.RESOLVED) ? "Ticket Resolved" : "Ticket Status Updated";
            String notifMsg = (newStatus == TicketStatus.RESOLVED)
                    ? "Ticket #" + savedTicket.getTicketNumber() + " has been marked as RESOLVED."
                    : "Ticket #" + savedTicket.getTicketNumber() + " status changed from " + oldStatus + " to " + newStatus + ".";
            notificationService.notifyTicketParticipants(savedTicket, actor, notifTitle, notifMsg, notifType);
        }

        return savedTicket;
    }

    /**
     * Add resolution notes and optionally mark the ticket as RESOLVED.
     * Avoids recording duplicate activity if resolution notes and status are already identical.
     */
    public Ticket addResolutionNotes(Long ticketId, String notes, boolean markResolved) {
        if (notes == null || notes.isBlank()) {
            throw new IllegalArgumentException("Resolution notes cannot be blank.");
        }

        Ticket ticket = getTicketById(ticketId);

        boolean statusUnchanged = !markResolved || ticket.getStatus() == TicketStatus.RESOLVED;
        boolean notesUnchanged = notes.trim().equals(ticket.getResolutionNotes());
        if (statusUnchanged && notesUnchanged) {
            // No state change occurs; avoid duplicate activity
            return ticket;
        }

        ticket.setResolutionNotes(notes.trim());

        if (markResolved) {
            ticket.setStatus(TicketStatus.RESOLVED);
        }

        ticket.setUpdatedAt(LocalDateTime.now());
        Ticket savedTicket = ticketRepository.save(ticket);

        // Record activity history with actual authenticated actor (or fallback to assigned technician/employee)
        Employee actor = securityUtils != null ? securityUtils.getCurrentEmployeeOrNull() : null;
        Employee performedBy = actor != null ? actor : (savedTicket.getAssignedTechnician() != null ? savedTicket.getAssignedTechnician() : savedTicket.getEmployee());
        ActivityType activityType = markResolved ? ActivityType.TICKET_RESOLVED : ActivityType.STATUS_CHANGED;
        String description = markResolved
                ? "Ticket marked as RESOLVED with notes: " + notes.trim()
                : "Resolution notes updated: " + notes.trim();

        recordActivity(savedTicket, performedBy, activityType, description);

        // Dispatch notifications to relevant participants
        if (notificationService != null) {
            NotificationType notifType = markResolved ? NotificationType.TICKET_RESOLVED : NotificationType.STATUS_CHANGED;
            String notifTitle = markResolved ? "Ticket Resolved" : "Resolution Notes Updated";
            String notifMsg = markResolved
                    ? "Ticket #" + savedTicket.getTicketNumber() + " has been marked as RESOLVED with notes: " + notes.trim()
                    : "Resolution notes updated for ticket #" + savedTicket.getTicketNumber() + ".";
            notificationService.notifyTicketParticipants(savedTicket, actor, notifTitle, notifMsg, notifType);
        }

        return savedTicket;
    }

    /**
     * Add a comment to a ticket using AddCommentRequest DTO.
     */
    public TicketComment addComment(Long ticketId, AddCommentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Comment request cannot be null.");
        }
        return addComment(ticketId, request.getEmployeeId(), request.getCommentText());
    }

    /**
     * Add a comment to a ticket given author employee ID and comment text.
     * Validates:
     * 1. Ticket exists (or throws 404).
     * 2. Author exists (or throws 404).
     * 3. Author is active (or throws 400).
     * 4. Comment text is not blank.
     * Records COMMENT_ADDED in ticket activity history.
     */
    public TicketComment addComment(Long ticketId, Long employeeId, String commentText) {
        if (employeeId == null) {
            throw new IllegalArgumentException("Employee ID is required.");
        }
        if (commentText == null || commentText.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment text cannot be empty.");
        }

        Ticket ticket = getTicketById(ticketId);

        Employee author = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        if (!author.isActive()) {
            throw new IllegalArgumentException("Cannot add comment as inactive employee (ID: " + employeeId + ").");
        }

        TicketComment comment = new TicketComment(ticket, author, commentText.trim());
        TicketComment savedComment = ticketCommentRepository.save(comment);

        // Record activity history
        recordActivity(ticket, author, ActivityType.COMMENT_ADDED,
                "Comment added by " + author.getName());

        // Dispatch notifications to relevant participants (author is excluded from receiving notification)
        if (notificationService != null) {
            String snippet = commentText.trim().length() > 60
                    ? commentText.trim().substring(0, 57) + "..."
                    : commentText.trim();
            String title = "New Comment on Ticket";
            String message = author.getName() + " commented on ticket #" + ticket.getTicketNumber() + ": \"" + snippet + "\"";
            notificationService.notifyTicketParticipants(ticket, author, title, message, NotificationType.COMMENT_ADDED);
        }

        return savedComment;
    }

    /**
     * List all comments for a ticket ordered chronologically (oldest first).
     * Throws 404 ResourceNotFoundException if ticket does not exist.
     */
    @Transactional(readOnly = true)
    public List<TicketComment> getCommentsByTicket(Long ticketId) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new ResourceNotFoundException("Ticket not found with ID: " + ticketId);
        }
        return ticketCommentRepository.findByTicketIdOrderByCreatedAtAscIdAsc(ticketId);
    }

    /**
     * List all activity records for a ticket ordered chronologically (oldest first).
     * Throws 404 ResourceNotFoundException if ticket does not exist.
     */
    @Transactional(readOnly = true)
    public List<TicketActivity> getTicketActivities(Long ticketId) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new ResourceNotFoundException("Ticket not found with ID: " + ticketId);
        }
        return ticketActivityRepository.findByTicketIdOrderByCreatedAtAscIdAsc(ticketId);
    }

    /**
     * Helper to persist an audit log entry for a ticket.
     */
    private void recordActivity(Ticket ticket, Employee performedBy, ActivityType activityType, String description) {
        TicketActivity activity = new TicketActivity(ticket, performedBy, activityType, description);
        ticketActivityRepository.save(activity);
    }

    /**
     * Calculates the SLA deadline based on priority.
     * Uses configurable hours (CRITICAL=4h, HIGH=8h, MEDIUM=24h, LOW=48h).
     */
    public LocalDateTime calculateSlaDeadline(TicketPriority priority, LocalDateTime baseTime) {
        LocalDateTime start = (baseTime != null) ? baseTime : LocalDateTime.now();
        TicketPriority prio = (priority != null) ? priority : TicketPriority.MEDIUM;
        return switch (prio) {
            case CRITICAL -> start.plusHours(criticalSlaHours);
            case HIGH -> start.plusHours(highSlaHours);
            case MEDIUM -> start.plusHours(mediumSlaHours);
            case LOW -> start.plusHours(lowSlaHours);
        };
    }

    /**
     * Returns the total SLA duration in hours for a given priority.
     */
    public long getSlaDurationHours(TicketPriority priority) {
        TicketPriority prio = (priority != null) ? priority : TicketPriority.MEDIUM;
        return switch (prio) {
            case CRITICAL -> criticalSlaHours;
            case HIGH -> highSlaHours;
            case MEDIUM -> mediumSlaHours;
            case LOW -> lowSlaHours;
        };
    }

    /**
     * Retrieve all tickets whose SLA deadline has passed and are not RESOLVED or CLOSED.
     */
    @Transactional(readOnly = true)
    public List<Ticket> getOverdueTickets() {
        return ticketRepository.findOverdueTickets(LocalDateTime.now());
    }

    /**
     * View a ticket's SLA deadline and overdue status.
     */
    @Transactional(readOnly = true)
    public SlaStatusResponse getTicketSlaStatus(Long ticketId) {
        Ticket ticket = getTicketById(ticketId);
        return new SlaStatusResponse(
                ticket.getId(),
                ticket.getTicketNumber(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getCreatedAt(),
                ticket.getSlaDeadline(),
                ticket.isOverdue(),
                getSlaDurationHours(ticket.getPriority())
        );
    }

    /**
     * Generates a collision-resistant unique ticket number.
     * Format: "TCK-YYYYMMDD-XXXXXX" (e.g., TCK-20261014-9F2B8D)
     * Does NOT rely on database count to prevent race condition collisions.
     */
    private String generateSafeTicketNumber() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String ticketNumber;
        do {
            String randomSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
            ticketNumber = "TCK-" + datePrefix + "-" + randomSuffix;
        } while (ticketRepository.existsByTicketNumber(ticketNumber));

        return ticketNumber;
    }
}
