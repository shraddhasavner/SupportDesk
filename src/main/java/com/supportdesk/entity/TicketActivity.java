package com.supportdesk.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * TicketActivity Entity: Maps to the 'ticket_activities' table in MySQL.
 * 
 * [English]:
 * Represents an immutable audit trail entry tracking actions performed on a ticket.
 * - @ManyToOne with Ticket: The ticket this event belongs to.
 * - @ManyToOne with Employee (performedBy): The employee or technician who performed the action (nullable for system actions).
 * - @Enumerated(EnumType.STRING) activityType: High-level classification of the event.
 * - description: Human-readable narrative of what occurred.
 * - createdAt: Exact timestamp of the event.
 * 
 * [Hinglish]:
 * Yeh class ticket ki audit history/timeline ko represent karti hai.
 * - `ticket`: Batata hai yeh activity kis ticket se judi hai.
 * - `performedBy`: Batata hai action kisne liya (employee/technician).
 * - `activityType`: Action ki category (TICKET_CREATED, STATUS_CHANGED, etc.).
 * - `description`: Aasan bhasha me detail (jaise "Ticket status changed from OPEN to ASSIGNED").
 */
@Entity
@Table(name = "ticket_activities")
public class TicketActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Ticket is required")
    @ManyToOne(optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    /**
     * The employee who performed the activity. Null if performed by automated system process.
     */
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee performedBy;

    @NotNull(message = "Activity type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 50)
    private ActivityType activityType;

    @NotBlank(message = "Description is required")
    @Column(nullable = false, length = 255)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Default constructor required by JPA.
     */
    public TicketActivity() {
    }

    /**
     * Parameterized constructor for convenient activity logging.
     */
    public TicketActivity(Ticket ticket, Employee performedBy, ActivityType activityType, String description) {
        this.ticket = ticket;
        this.performedBy = performedBy;
        this.activityType = activityType;
        this.description = description;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // ==================== Getters & Setters ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    /**
     * Convenience helper for clients to get ticket ID directly.
     */
    @JsonProperty("ticketId")
    public Long getTicketId() {
        return ticket != null ? ticket.getId() : null;
    }

    public Employee getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(Employee performedBy) {
        this.performedBy = performedBy;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public void setActivityType(ActivityType activityType) {
        this.activityType = activityType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "TicketActivity{" +
                "id=" + id +
                ", ticketId=" + (ticket != null ? ticket.getId() : null) +
                ", performedBy=" + (performedBy != null ? performedBy.getName() : "System") +
                ", activityType=" + activityType +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
